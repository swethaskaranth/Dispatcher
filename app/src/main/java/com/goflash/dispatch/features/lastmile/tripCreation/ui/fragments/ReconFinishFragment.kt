package com.goflash.dispatch.features.lastmile.tripCreation.ui.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.goflash.dispatch.SortationApplication
import com.goflash.dispatch.data.TripDTO
import com.goflash.dispatch.databinding.LayoutTripListBinding
import com.goflash.dispatch.di.components.DaggerFragmemntComponent
import com.goflash.dispatch.features.lastmile.tripCreation.listeners.FragmentListener
import com.goflash.dispatch.features.lastmile.tripCreation.listeners.SelectedListener
import com.goflash.dispatch.features.lastmile.tripCreation.presenter.ReconFinishPresenter
import com.goflash.dispatch.features.lastmile.tripCreation.ui.activity.LastMileActivity
import com.goflash.dispatch.features.lastmile.tripCreation.ui.activity.LastMileSummaryActivity
import com.goflash.dispatch.features.lastmile.tripCreation.ui.adapter.OfdTripAdapter
import com.goflash.dispatch.features.lastmile.tripCreation.view.ReconFinishView
import com.goflash.dispatch.ui.fragments.BaseFragment
import javax.inject.Inject

class ReconFinishFragment : BaseFragment(), ReconFinishView, SelectedListener,
    LastMileActivity.RefreshPage {

    @Inject
    lateinit var mPresenter: ReconFinishPresenter
    lateinit var v: View

    private var fragmentToActivity: FragmentListener? = null

    private var list = mutableListOf<TripDTO>()

    private lateinit var binding: LayoutTripListBinding

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            fragmentToActivity = context as LastMileActivity
            (activity as LastMileActivity).refreshPage.add(this)
        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement RefreshPage")
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initDagger()
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
            .build().inject(this@ReconFinishFragment)

        mPresenter.onAttachView(requireActivity(), this)
    }


    fun getTrips() {
        mPresenter.getReconFinishTrips()
    }

    override fun onFailure(error: Throwable?) {

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

        LastMileActivity.rfCount = tripList.size
        fragmentToActivity?.commonListener()

        binding.rvTrip.adapter?.notifyDataSetChanged()

    }

    override fun refreshPage() {
        getTrips()
    }

    override fun onCallDeliveryAgent(number: String) {

    }

    override fun onShipmentSelected(position: Int, view: Int) {
        val intent = Intent(activity, LastMileSummaryActivity::class.java)
        intent.putExtra("tripId", list[position].tripId.toString())
        intent.putExtra("sprinterName", list[position].agentName)
        intent.putExtra("cdsCashCollection", list[position].cdsCashCollection)
        startActivity(intent)
    }

    override fun onStop() {
        super.onStop()
        mPresenter.onDetachView()
    }
}
