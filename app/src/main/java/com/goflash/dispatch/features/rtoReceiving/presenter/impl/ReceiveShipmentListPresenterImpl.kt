package com.goflash.dispatch.features.rtoReceiving.presenter.impl

import android.content.Context
import com.goflash.dispatch.api_services.SessionService
import com.goflash.dispatch.di.interactor.SortationApiInteractor
import com.goflash.dispatch.features.rtoReceiving.presenter.ReceiveShipmentListPresenter
import com.goflash.dispatch.features.rtoReceiving.view.ReceiveShipmentsListView
import com.goflash.dispatch.util.PreferenceHelper
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription

class ReceiveShipmentListPresenterImpl(private val sortationApiInteractor: SortationApiInteractor) :
    ReceiveShipmentListPresenter {

    private var mView: ReceiveShipmentsListView? = null
    private var compositeSubscription: CompositeSubscription? = null

    override fun onAttach(context: Context, view: ReceiveShipmentsListView) {
        this.mView = view
        compositeSubscription = CompositeSubscription()
        getInwardRuns()
    }

    private fun getInwardRuns() {
        compositeSubscription?.add(
            sortationApiInteractor.getInwardRuns(PreferenceHelper.dataForNumDays)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    response.completed?.let { mView?.onInwardRunsFetched(it) }


                }, {
                    mView?.onFailure(it)
                })
        )
    }

    override fun onDetach() {
        if (mView == null)
            return
        mView = null
        compositeSubscription?.unsubscribe()
        compositeSubscription = null
    }
}