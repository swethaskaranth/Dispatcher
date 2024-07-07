package com.goflash.dispatch.features.lastmile.tripCreation.presenter.impl

import android.content.Context
import com.goflash.dispatch.api_services.SessionService
import com.goflash.dispatch.data.TripDTO
import com.goflash.dispatch.di.interactor.SortationApiInteractor
import com.goflash.dispatch.features.lastmile.tripCreation.presenter.CreatedPresenter
import com.goflash.dispatch.features.lastmile.tripCreation.view.CreatedView
import com.goflash.dispatch.features.lastmile.tripCreation.view.StatusRowView
import com.goflash.dispatch.model.ActiveTrips
import com.goflash.dispatch.type.BagStatus
import com.goflash.dispatch.util.PreferenceHelper
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription

class CreatedPresenterImpl(private val sortationApiInteractor: SortationApiInteractor) :
    CreatedPresenter {

    private var view: CreatedView? = null

    private var compositeSubscription: CompositeSubscription? = null

    private var tripList = mutableListOf<TripDTO>()

    private var apiCalled = false

    override fun onAttachView(context: Context, view: CreatedView) {
        this.view = view
        compositeSubscription = CompositeSubscription()
    }

    override fun onDetachView() {
        if (view == null)
            return
        view = null
        compositeSubscription?.unsubscribe()
        compositeSubscription = null
    }

    override fun getCreatedTrips() {
        if(!apiCalled) {
            apiCalled = true
            compositeSubscription?.add(
                sortationApiInteractor.getParticularTrips(request())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        tripList.clear()
                        tripList.addAll(it)
                        view?.onSuccess(it)
                        apiCalled = false
                    }, {
                        view?.onFailure(it)
                        apiCalled = false
                    })
            )
        }
    }

    private fun request(): ActiveTrips {

        return ActiveTrips(BagStatus.CREATED.name, PreferenceHelper.startDate, PreferenceHelper.endDate,
            agentName = PreferenceHelper.agentName,  excludedTrips = mutableListOf(), routeId = PreferenceHelper.routeId)

    }

    override fun getBagList() {

    }

    override fun getCount(): Int {
        return tripList.size
    }

    override fun onStatusRowView(position: Int, holder: StatusRowView) {
        val data = tripList[position]

        holder.bagId(data.tripId)
        holder.setName(data.agentName, data.routeId)
        holder.setBin(data.bin)
        holder.setCount(data.taskCount.toString())
        holder.onClickListner(position, data)
        holder.onListClick(position)

        if(tripList.size <=1)
            holder.hideMerge()

        holder.enableOrDisableClicks(!SessionService.selfAssignment)

    }

    override fun onClickListner(id: Int, screen: Int) {
        if (!SessionService.selfAssignment)
            if (screen == 1)
                view?.onListClick(tripList[id])
            else
                view?.onEditClick(tripList[id], screen)
    }

    override fun onMergeClicked(position: Int) {
        view?.onMergeClick(tripList[position].tripId)
    }

}