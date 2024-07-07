package com.goflash.dispatch.features.lastmile.tripCreation.presenter.impl

import android.content.Context
import com.goflash.dispatch.di.interactor.SortationApiInteractor
import com.goflash.dispatch.features.lastmile.tripCreation.presenter.UnblockPresenter
import com.goflash.dispatch.features.lastmile.tripCreation.view.UnblockView
import com.goflash.dispatch.model.DeleteShipment
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription

class UnblockPresenterImpl(private val sortationApiInteractor: SortationApiInteractor) : UnblockPresenter {

    private var view: UnblockView? = null

    private var compositeSubscription: CompositeSubscription? = null

    override fun onAttachView(context: Context, view: UnblockView) {
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

     override fun unBlockShipement(shipmentId: String, referenceId: String) {
         compositeSubscription?.add(sortationApiInteractor.unBlockShipment(request(shipmentId, referenceId))
             .subscribeOn(Schedulers.io())
             .observeOn(AndroidSchedulers.mainThread())
             .subscribe({
                 view?.onSuccess()
             }, {
                 view?.onFailure(it)
             }))
    }

    private fun request(shipmentId: String, referenceId: String): DeleteShipment {
        return DeleteShipment(shipmentId = shipmentId, referenceId = referenceId)
    }
}