package com.goflash.dispatch.features.lastmile.tripCreation.ui.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import co.uk.rushorm.core.RushSearch
import com.goflash.dispatch.R
import com.goflash.dispatch.SortationApplication
import com.goflash.dispatch.data.TripDTO
import com.goflash.dispatch.data.TripSettlementDTO
import com.goflash.dispatch.databinding.LayoutTripListBinding
import com.goflash.dispatch.di.components.DaggerFragmemntComponent
import com.goflash.dispatch.features.lastmile.settlement.ui.activity.*

import com.goflash.dispatch.features.lastmile.tripCreation.listeners.FragmentListener
import com.goflash.dispatch.features.lastmile.tripCreation.listeners.SelectedListener
import com.goflash.dispatch.features.lastmile.tripCreation.presenter.CompletedPresenter
import com.goflash.dispatch.features.lastmile.tripCreation.ui.activity.LastMileActivity
import com.goflash.dispatch.features.lastmile.tripCreation.ui.activity.LastMileSummaryActivity
import com.goflash.dispatch.features.lastmile.tripCreation.ui.adapter.OfdTripAdapter
import com.goflash.dispatch.features.lastmile.tripCreation.view.CompletedView
import com.goflash.dispatch.ui.fragments.BaseFragment
import javax.inject.Inject

class CompletedFragment : BaseFragment(), CompletedView, SelectedListener,
    LastMileActivity.RefreshPage {

    @Inject
    lateinit var mPresenter: CompletedPresenter
    lateinit var v: View

    private var fragmentToActivity: FragmentListener? = null

    private var list = mutableListOf<TripDTO>()
    private var tripId: String? = null
    private var sprinterName: String? = null

    private var tripList: TripSettlementDTO? = null

    private lateinit var binding: LayoutTripListBinding

    override fun onAttach(context: Context) {
        super.onAttach(context)
        initDagger()
        try {
            fragmentToActivity = context as LastMileActivity
            (activity as LastMileActivity).refreshPage.add(this)
        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement RefreshPage")
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = LayoutTripListBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        v = view
        binding.rvTrip.layoutManager = LinearLayoutManager(requireActivity())
        binding.rvTrip.adapter = OfdTripAdapter(requireActivity(), list, this)
        binding.shimmerViewContainer.startShimmer()


        getTrips()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }


    private fun initDagger() {
        DaggerFragmemntComponent.builder()
            .networkComponent((requireActivity().application as SortationApplication).getNetworkComponent())
            .build().inject(this@CompletedFragment)

        mPresenter.onAttachView(requireActivity(), this)
    }


    fun getTrips() {
        mPresenter.getCompletedTrips()
    }

    override fun onSuccess(tripList: List<TripDTO>) {

        binding.shimmerViewContainer.stopShimmer()
        binding.shimmerViewContainer.visibility = View.GONE
        if (tripList.isEmpty())
            binding.tvEmptyText.visibility = View.VISIBLE
        else
            binding.rvTrip.visibility = View.VISIBLE

        list.clear()
        list.addAll(tripList)

        LastMileActivity.completedCount = tripList.size
        fragmentToActivity?.commonListener()

        /*RushCore.getInstance().delete(
            RushSearch().whereEqual(STATUS, BagStatus.COMPLETED.name).or().whereEqual(STATUS, BagStatus.RECON_STARTED.name)
                .find(TripDTO::class.java))

        tripList.forEach {
            it.save()
        }*/

        binding.rvTrip.adapter?.notifyDataSetChanged()
    }

    override fun onSuccessSettlement(tripSettlementDTO: TripSettlementDTO, tripId: Long) {
        hideProgress()

        val data =
            RushSearch().whereEqual("tripId", tripId).findSingle(TripSettlementDTO::class.java)
        if (data == null) {
            tripSettlementDTO.tripId = tripId

            for (shipment in tripSettlementDTO.returnShipment)
                shipment.status = "Pending"

            tripSettlementDTO.save()
            tripSettlementDTO.receiveCash.save()
            tripSettlementDTO.receiveCheque.save()
            tripSettlementDTO.receiveNetBank.save()

            for ((_, shipment) in tripSettlementDTO.fmPickedupShipments) {
                shipment.map { it.tripId = tripId }
                shipment.forEach { it.save() }
            }

            for ((lbn, ackRecon) in tripSettlementDTO.ackSlips) {
                ackRecon.lbn = lbn
                ackRecon.tripId = tripId
                ackRecon.save()
            }
        }

        redirectActivity(tripSettlementDTO)
    }

    override fun onFailure(error: Throwable?) {
        hideProgress()
        processError(error)
    }

    override fun refreshPage() {
        getTrips()
    }

    override fun onCallDeliveryAgent(number: String) {
        fragmentToActivity?.callDeliveryAgent(number)
    }

    override fun onShipmentSelected(position: Int, view: Int) {

        tripId = list[position].tripId.toString()
        sprinterName = list[position].agentName

        if (view == 2) {
            showProgress()
            mPresenter.getReconStartedTrip(list[position].tripId)
            return
        }

        val intent = Intent(activity, LastMileSummaryActivity::class.java)
        intent.putExtra("tripId", tripId)
        intent.putExtra("sprinterName", sprinterName)
        intent.putExtra("cdsCashCollection", list[position].cdsCashCollection)
        startActivity(intent)
    }

    private fun redirectActivity(tripSettlementDTO: TripSettlementDTO) {

        tripList = mPresenter.getUndeliveredData(tripId!!)

        val intent = when {
            (tripSettlementDTO.undeliveredShipment.size != 0 && !tripList?.isUndeliveredScanned!!) || tripSettlementDTO.lostDamagedShipments.size != 0 -> Intent(
                activity,
                Step1UndeliveredActivity::class.java
            )
            tripSettlementDTO.returnShipment.size != 0 && !tripList?.isReturnScanned!! -> Intent(
                activity,
                Step2PickedUpActivity::class.java
            )
            tripSettlementDTO.fmPickedupShipments.isNotEmpty() && !(tripList?.isFmPickupScanned
                ?: false) -> Intent(activity, ReceiveFmPickupShipmentActivity::class.java)
            tripSettlementDTO.ackSlips.isNotEmpty() -> Intent(
                activity,
                Step4VerifyImagesActivity::class.java
            )
            tripSettlementDTO.poas.isNotEmpty() && !(tripList?.isPoasCaptured
                ?: false)-> Intent(activity, AckDeliverySlipReconActivity::class.java)
            else -> Intent(activity, Step3CashCollectionActivity::class.java)
        }

        intent.putExtra("tripId", tripId)
        intent.putExtra("sprinterName", sprinterName)
        startActivity(intent)
    }

    override fun onStop() {
        super.onStop()

    }

    override fun onDetach() {
        super.onDetach()
        mPresenter.onDetachView()
    }

}
