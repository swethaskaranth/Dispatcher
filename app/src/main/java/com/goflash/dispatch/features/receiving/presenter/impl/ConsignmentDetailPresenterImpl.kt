package com.goflash.dispatch.features.receiving.presenter.impl

import android.content.Context
import com.goflash.dispatch.di.interactor.SortationApiInteractor
import com.goflash.dispatch.features.receiving.presenter.ConsignmentDetailPresenter
import com.goflash.dispatch.features.receiving.view.ConsignmentDetailView
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription

class ConsignmentDetailPresenterImpl (private val sortationApiInteractor: SortationApiInteractor) : ConsignmentDetailPresenter{

    private var detailView: ConsignmentDetailView? = null

    private var compositeSubscription : CompositeSubscription? = null



    override fun onAttachView(context: Context, consignmentDetailView: ConsignmentDetailView) {
        this.detailView = consignmentDetailView
        compositeSubscription = CompositeSubscription()
    }


    override fun onDetachView() {
        if(detailView == null)
            return
        detailView = null
        compositeSubscription?.unsubscribe()
        compositeSubscription = null
    }


    override fun getBagShipments(tripId: Long) {
        compositeSubscription?.add(sortationApiInteractor.getBagShipments(tripId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({list ->

                    detailView?.onSuccess(list)

                },{error ->
                    detailView?.onFailure(error)

                }))
    }


}