package com.goflash.dispatch.features.lastmile.tripCreation.presenter.impl

import android.content.Context
import com.goflash.dispatch.di.interactor.SortationApiInteractor
import com.goflash.dispatch.features.lastmile.tripCreation.presenter.ScanShipmentPresenter
import com.goflash.dispatch.features.lastmile.tripCreation.view.ScanShipmentView
import com.goflash.dispatch.features.lastmile.tripCreation.view.SummaryDetailView
import com.goflash.dispatch.model.CommonRequest
import com.goflash.dispatch.model.DeleteTrip
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription

class SummaryDetailPresenterImpl (private val sortationApiInteractor: SortationApiInteractor) : SummaryDetailPresenter {

    private var mView: SummaryDetailView? = null
    private var compositeSubscription: CompositeSubscription? = null

    override fun onAttach(context: Context, view: SummaryDetailView) {
        this.mView = view
        compositeSubscription = CompositeSubscription()
    }

    override fun onDetach() {
        if (mView == null)
            return
        mView = null
        compositeSubscription?.unsubscribe()
        compositeSubscription = null
    }

    override fun getAddressDetails(position: Int, shipmentId: String) {
        compositeSubscription?.add(sortationApiInteractor.viewAddressDetails(shipmentId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                mView?.updateList(position, it)
            },{
                mView?.onFailure(it)
            }))
    }
}