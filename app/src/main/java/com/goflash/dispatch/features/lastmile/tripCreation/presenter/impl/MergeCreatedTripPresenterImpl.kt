package com.goflash.dispatch.features.lastmile.tripCreation.presenter.impl

import android.content.Context
import com.goflash.dispatch.R
import com.goflash.dispatch.data.MergeCreatedTripRequest
import com.goflash.dispatch.data.TripDTO
import com.goflash.dispatch.di.interactor.SortationApiInteractor
import com.goflash.dispatch.features.lastmile.tripCreation.presenter.MergeCreatedTripPresenter
import com.goflash.dispatch.features.lastmile.tripCreation.view.MergeCreatedTripView
import com.goflash.dispatch.features.lastmile.tripCreation.view.MergeTripRowView
import com.goflash.dispatch.model.ActiveTrips
import com.goflash.dispatch.type.BagStatus
import com.goflash.dispatch.util.PreferenceHelper
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription

class MergeCreatedTripPresenterImpl(val sortationApiInteractor: SortationApiInteractor) :
    MergeCreatedTripPresenter {

    private var context: Context? = null
    private var mView: MergeCreatedTripView? = null

    private var compositeSubscription: CompositeSubscription? = null

    private val createdTrips: MutableList<TripDTO> = mutableListOf()
    private val filteredTrips: MutableList<TripDTO> = mutableListOf()

    private var fromTripId: Long = -1

    private var selectedTrip = -1

    private var onBind = false

    private var filterApplied = false

    override fun onAttachView(context: Context, view: MergeCreatedTripView) {
        this.context = context
        this.mView = view
        compositeSubscription = CompositeSubscription()

    }

    override fun onDetachView() {
        if (mView == null)
            return
        mView = null
        compositeSubscription?.unsubscribe()
        compositeSubscription = null
    }

    override fun getTrips(excludedTripId: Long) {
        val request = ActiveTrips(
            BagStatus.CREATED.name,
            PreferenceHelper.startDate,
            PreferenceHelper.endDate,
            agentName = PreferenceHelper.agentName,
            excludedTrips = mutableListOf()
        )
        fromTripId = excludedTripId

        compositeSubscription?.add(
            sortationApiInteractor.getParticularTrips(request)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ list ->
                    val excludedTrip = list.find { it.tripId == excludedTripId }
                    val tripList = list.toMutableList()
                    tripList.remove(excludedTrip)
                    createdTrips.clear()
                    createdTrips.addAll(tripList)
                    mView?.refreshList()
                }, { error ->
                    mView?.onFailure(error)

                })
        )
    }

    override fun getCount(): Int {
        return if (filterApplied) filteredTrips.size else createdTrips.size
    }

    override fun bindViewHolder(position: Int, holder: MergeTripRowView) {
        onBind = true
        val trip = if (filterApplied) filteredTrips[position] else createdTrips[position]

        holder.setTripId(String.format(context!!.getString(R.string.trip_with_id), trip.tripId))
        holder.setCount(trip.taskCount)
        holder.setSprinterName(trip.agentName)
        holder.setRadioOnChangeListener(position)

        holder.setRadio(position == selectedTrip)

        onBind = false

    }

    override fun onTripSelected(position: Int) {
        if (!onBind) {
            selectedTrip = position
            mView?.refreshList()
            mView?.enableMerge(true)
        }
    }

    override fun mergeTrips() {
        compositeSubscription?.add(
            sortationApiInteractor.mergeCreatedTrips(
                MergeCreatedTripRequest(
                    fromTripId.toString(),
                    if (filterApplied)
                        filteredTrips[selectedTrip].tripId.toString()
                    else
                        createdTrips[selectedTrip].tripId.toString()
                )
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    mView?.onSuccess()
                }, {
                    mView?.onFailure(it)
                })
        )
    }

    override fun filterSprinters(str: String) {
        filteredTrips.clear()
        filterApplied = true
        selectedTrip = -1
        mView?.enableMerge(false)
        filteredTrips.addAll(createdTrips.filter {
            it.tripId.toString().contains(str, true) || it.agentName?.contains(str, true) == true
        })
        mView?.refreshList()


    }

    override fun clearFilter() {
        filterApplied = false
        selectedTrip = -1
        filteredTrips.clear()
        mView?.refreshList()
        mView?.enableMerge(false)

    }


}