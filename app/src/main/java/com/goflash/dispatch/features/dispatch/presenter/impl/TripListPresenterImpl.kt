package com.goflash.dispatch.features.dispatch.presenter.impl

import android.content.Context
import com.goflash.dispatch.data.ReceivingDto
import com.goflash.dispatch.di.interactor.SortationApiInteractor
import com.goflash.dispatch.features.dispatch.presenter.TripListPresenter
import com.goflash.dispatch.features.dispatch.view.TripListView
import com.goflash.dispatch.features.dispatch.view.TripRowView
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription
import java.text.SimpleDateFormat
import java.util.*

class TripListPresenterImpl (private val sortationApiInteractor: SortationApiInteractor) : TripListPresenter {

    private val TAG = TripListPresenterImpl::class.java.name

    private var tripListView : TripListView? = null

    private var compositeSubscription : CompositeSubscription? = null

    private val tripList = mutableListOf<ReceivingDto>()

    override fun onAttachView(context: Context, view: TripListView) {
        this.tripListView = view
        compositeSubscription = CompositeSubscription()
    }

    override fun onDetachView() {
        if(tripListView == null)
            return
        tripListView = null
        compositeSubscription?.unsubscribe()
        compositeSubscription = null
    }

    override fun getTripList() {
        compositeSubscription?.add(sortationApiInteractor.getTripList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({list ->
                    tripList.addAll(list)
                    tripListView?.refreshList()

                },{error ->
                    tripListView?.onFailure(error)

                }))
    }

    override fun getCount(): Int {
        return tripList.size
    }

    override fun onBindTripRowView(position: Int, holder: TripRowView) {
        val trip = tripList[position]

        holder.setTripId(trip.tripId.toString())
        holder.setSprinterName(trip.agentName)
        holder.setTripStatus(trip.status)
        holder.setTripDestination(trip.assetName)
        holder.setTripDate(getTimeFromISODate(trip.createdOn))

    }

    private fun getTimeFromISODate(dateStr: String): String {

        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        val cal = Calendar.getInstance()
        format.timeZone = TimeZone.getTimeZone("IST")
        cal.time = format.parse(dateStr)


        val dateFormat = SimpleDateFormat("dd-MM-yy, hh:mm a")
        val dateforrow = dateFormat.format(cal.time)

        return dateforrow

    }


}