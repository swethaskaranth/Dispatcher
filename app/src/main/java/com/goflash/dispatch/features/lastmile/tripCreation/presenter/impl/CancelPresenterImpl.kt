package com.goflash.dispatch.features.lastmile.tripCreation.presenter.impl

import android.content.Context
import com.goflash.dispatch.di.interactor.SortationApiInteractor
import com.goflash.dispatch.features.lastmile.tripCreation.presenter.CancelPresenter
import com.goflash.dispatch.features.lastmile.tripCreation.view.CancelView
import com.goflash.dispatch.model.DeleteShipment
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription

class CancelPresenterImpl(private val sortationApiInteractor: SortationApiInteractor) : CancelPresenter {

    private var view: CancelView? = null

    private var compositeSubscription: CompositeSubscription? = null

    override fun onAttachView(context: Context, view: CancelView) {
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

     override fun cancelShipement(reason: String, shipmentId: String) {
         compositeSubscription?.add(sortationApiInteractor.deleteShipment(request(reason, shipmentId))
             .subscribeOn(Schedulers.io())
             .observeOn(AndroidSchedulers.mainThread())
             .subscribe({
                 view?.onSuccess()
             }, {
                 view?.onFailure(it)
             }))
    }

    private fun request(reason: String, shipmentId: String): DeleteShipment {
        return DeleteShipment(reason = reason, shipmentId =  shipmentId)
    }
}