package com.goflash.dispatch.features.lastmile.tripCreation.presenter.impl

import android.content.Context
import com.goflash.dispatch.data.TripCount
import com.goflash.dispatch.di.interactor.SortationApiInteractor
import com.goflash.dispatch.features.lastmile.tripCreation.presenter.LastMilePresenter
import com.goflash.dispatch.features.lastmile.tripCreation.ui.activity.LastMileActivity
import com.goflash.dispatch.features.lastmile.tripCreation.view.LastMileView
import com.goflash.dispatch.type.BagStatus
import com.goflash.dispatch.util.PreferenceHelper
import com.goflash.dispatch.util.getTimestampString
import com.goflash.dispatch.util.getTimestampString2
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription

class LastMilePresenterImpl(private val sortationApiInteractor: SortationApiInteractor) :
    LastMilePresenter {

    private var view: LastMileView? = null

    private var compositeSubscription: CompositeSubscription? = null

    private var smartTripId = -1

    override fun onAttachView(context: Context, view: LastMileView) {
        this.view = view
        compositeSubscription = CompositeSubscription()
    }

    override fun onDetachView() {
        if (view == null)
            return
        view = null
        compositeSubscription?.unsubscribe()
        compositeSubscription = null
        PreferenceHelper.agentName = null
    }

    override fun getUnassignedCount() {
        compositeSubscription?.add(sortationApiInteractor.unAssignedCount()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                view?.onSuccess(it)
            }, {
                view?.onFailure(it)
            })
        )
    }

    override fun getTabCount() {

        compositeSubscription?.add(sortationApiInteractor.getTabCount(getTimestampString(6), getTimestampString2())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                setCount(it)
            }, {
                view?.onFailure(it)
            })
        )
    }

    private fun setCount(tripCount: List<TripCount>) {
        tripCount.forEach {
            if (it.status == BagStatus.CREATED.name)
                LastMileActivity.createdCount = it.count
            if (it.status == BagStatus.OUT_FOR_DELIVERY.name)
                LastMileActivity.ofdCount = it.count
            if (it.status == BagStatus.COMPLETED.name)
                LastMileActivity.completedCount = it.count
            if (it.status == BagStatus.RECON_FINISHED.name)
                LastMileActivity.rfCount = it.count
        }

        view?.onTabCount()
    }

    override fun cancelSmartTrip() {
        compositeSubscription?.add(sortationApiInteractor.cancelSmartTrip(smartTripId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                smartTripId = -1
                view?.onCancelSuccess()
            }, {

                view?.onFailure(it)
            })
        )
    }
}