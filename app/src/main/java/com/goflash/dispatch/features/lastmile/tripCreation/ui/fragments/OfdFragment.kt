package com.goflash.dispatch.features.lastmile.tripCreation.ui.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.goflash.dispatch.R
import com.goflash.dispatch.SortationApplication
import com.goflash.dispatch.data.TripDTO
import com.goflash.dispatch.databinding.LayoutTripListBinding
import com.goflash.dispatch.di.components.DaggerFragmemntComponent
import com.goflash.dispatch.features.lastmile.tripCreation.listeners.FragmentListener
import com.goflash.dispatch.features.lastmile.tripCreation.listeners.SelectedListener
import com.goflash.dispatch.features.lastmile.tripCreation.presenter.OfdPresenter
import com.goflash.dispatch.features.lastmile.tripCreation.ui.activity.LastMileActivity
import com.goflash.dispatch.features.lastmile.tripCreation.ui.activity.LastMileActivity.*
import com.goflash.dispatch.features.lastmile.tripCreation.ui.activity.LastMileSummaryActivity
import com.goflash.dispatch.features.lastmile.tripCreation.ui.adapter.OfdTripAdapter
import com.goflash.dispatch.features.lastmile.tripCreation.view.OfdView
import com.goflash.dispatch.ui.fragments.BaseFragment
import javax.inject.Inject

@Suppress("UNCHECKED_CAST")
class OfdFragment : BaseFragment(), OfdView, SelectedListener, RefreshPage {

    @Inject
    lateinit var mPresenter: OfdPresenter
    lateinit var v: View

    private var fragmentToActivity: FragmentListener? = null

    private var list = mutableListOf<TripDTO>()

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
        binding.rvTrip.layoutManager = LinearLayoutManager(activity)
        binding.rvTrip.adapter = OfdTripAdapter(requireActivity(), list, this)
        binding.shimmerViewContainer.startShimmer()

        getTrips()

    }

    private fun initDagger() {
        DaggerFragmemntComponent.builder()
            .networkComponent((requireActivity().application as SortationApplication).getNetworkComponent())
            .build().inject(this@OfdFragment)

        mPresenter.onAttachView(requireActivity(), this)
    }

    override fun onResume() {
        super.onResume()

    }

    fun getTrips() {
        mPresenter.getOfdTrips()
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

        LastMileActivity.ofdCount = tripList.size
        fragmentToActivity?.commonListener()

        /*RushCore.getInstance().delete(
            RushSearch().whereEqual(STATUS, BagStatus.OUT_FOR_DELIVERY.name).find(TripDTO::class.java))

        tripList.forEach {
            it.save()
        }*/

        binding.rvTrip.adapter?.notifyDataSetChanged()

    }

    override fun onFailure(error: Throwable?) {
        processError(error)
    }

    override fun refreshPage() {
            getTrips()
    }

    override fun onShipmentSelected(position: Int, view: Int) {
        val intent = Intent(activity, LastMileSummaryActivity::class.java)
        intent.putExtra("tripId", list[position].tripId.toString())
        intent.putExtra("sprinterName", list[position].agentName)
        intent.putExtra("cdsCashCollection", list[position].cdsCashCollection)
        intent.putExtra("OFD", true)
        startActivity(intent)
    }

    override fun onCallDeliveryAgent(number: String) {
        fragmentToActivity?.callDeliveryAgent(number)
    }

    override fun onDetach() {
        super.onDetach()
        mPresenter.onDetachView()
    }
}
