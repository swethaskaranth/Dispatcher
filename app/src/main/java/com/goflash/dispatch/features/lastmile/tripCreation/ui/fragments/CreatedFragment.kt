package com.goflash.dispatch.features.lastmile.tripCreation.ui.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.goflash.dispatch.SortationApplication
import com.goflash.dispatch.app_constants.sprinter_name
import com.goflash.dispatch.app_constants.trip_id
import com.goflash.dispatch.app_constants.trip_status
import com.goflash.dispatch.data.TripDTO
import com.goflash.dispatch.databinding.LayoutTripListBinding
import com.goflash.dispatch.di.components.DaggerFragmemntComponent
import com.goflash.dispatch.features.lastmile.tripCreation.listeners.FragmentListener
import com.goflash.dispatch.features.lastmile.tripCreation.presenter.CreatedPresenter
import com.goflash.dispatch.features.lastmile.tripCreation.ui.activity.LastMileActivity
import com.goflash.dispatch.features.lastmile.tripCreation.ui.activity.LastMileActivity.RefreshPage
import com.goflash.dispatch.features.lastmile.tripCreation.ui.activity.MergeCreatedTripActivity
import com.goflash.dispatch.features.lastmile.tripCreation.ui.activity.ScanShipmentActivity
import com.goflash.dispatch.features.lastmile.tripCreation.ui.activity.SelectSprinterActivity
import com.goflash.dispatch.features.lastmile.tripCreation.ui.adapter.CreatedTripAdapter
import com.goflash.dispatch.features.lastmile.tripCreation.view.CreatedView
import com.goflash.dispatch.ui.fragments.BaseFragment
import javax.inject.Inject

class CreatedFragment : BaseFragment(), CreatedView, RefreshPage {

    @Inject
    lateinit var mPresenter: CreatedPresenter

    private var fragmentToActivity: FragmentListener? = null

    lateinit var v: View

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
        binding = LayoutTripListBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        v = view
        binding.rvTrip.layoutManager = LinearLayoutManager(activity)
        binding.rvTrip.adapter = CreatedTripAdapter(requireActivity(), mPresenter)
        binding.shimmerViewContainer.startShimmer()

        getTrips()

    }

    private fun initDagger() {
        DaggerFragmemntComponent.builder()
            .networkComponent((requireActivity().application as SortationApplication).getNetworkComponent())
            .build().inject(this@CreatedFragment)

        mPresenter.onAttachView(requireActivity(), this)
    }

    private fun getTrips() {
        mPresenter.getCreatedTrips()
    }

    override fun onSuccess(tripList: List<TripDTO>) {

        binding.shimmerViewContainer.stopShimmer()
        binding.shimmerViewContainer.visibility = View.GONE
        if (tripList.isEmpty())
            binding.tvEmptyText.visibility = View.VISIBLE
        else
            binding.rvTrip.visibility = View.VISIBLE

        LastMileActivity.createdCount = tripList.size
        fragmentToActivity?.commonListener()

        binding.rvTrip.adapter?.notifyDataSetChanged()

    }

    override fun onFailure(error: Throwable?) {
        hideProgress()
    }

    override fun onEditClick(data: TripDTO, screen: Int) {
        val intent = Intent(activity, SelectSprinterActivity::class.java)
        intent.putExtra(trip_id, data.tripId)
        intent.putExtra(sprinter_name, data.agentName)
        intent.putExtra(trip_status, "CREATED")
        startActivity(intent)
    }

    override fun onListClick(data: TripDTO) {
        val intent = Intent(activity, ScanShipmentActivity::class.java)
        intent.putExtra(trip_id, data.tripId)
        intent.putExtra(sprinter_name, data.agentName)
        startActivity(intent)
    }

    override fun onMergeClick(data: Long) {
        val intent = Intent(activity, MergeCreatedTripActivity::class.java)
        intent.putExtra(trip_id, data)
        startActivity(intent)
    }

    override fun refreshPage() {
        getTrips()
    }

    override fun onDetach() {
        super.onDetach()
        mPresenter.onDetachView()
    }
}
