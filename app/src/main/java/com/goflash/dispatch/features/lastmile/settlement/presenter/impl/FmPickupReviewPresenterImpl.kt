package com.goflash.dispatch.features.lastmile.settlement.presenter.impl

import android.content.Context
import co.uk.rushorm.core.RushSearch
import com.goflash.dispatch.data.FmPickedShipment
import com.goflash.dispatch.di.interactor.SortationApiInteractor
import com.goflash.dispatch.features.lastmile.settlement.presenter.FmPickupReviewPresenter
import com.goflash.dispatch.features.lastmile.settlement.view.FmPickupReviewView
import rx.subscriptions.CompositeSubscription

class FmPickupReviewPresenterImpl(private val sortationApiInteractor: SortationApiInteractor): FmPickupReviewPresenter {

    private var mView: FmPickupReviewView? = null

    private var compositeSubscription: CompositeSubscription? = null

    private val fmShipments : MutableList<FmPickedShipment> = mutableListOf()

    override fun onAttachView(context: Context, view: FmPickupReviewView) {
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

    override fun getShipments(originName: String) {
        val shipments = RushSearch().whereEqual("originName",originName)
            .and().whereEqual("scanned",false)
            .find(FmPickedShipment::class.java)
        if(!shipments.isNullOrEmpty())
            fmShipments.addAll(shipments)

        mView?.onShipmentsFetched(fmShipments)


    }

    override fun onReasonSelected(position: Int, reason: String) {
        val shipment = fmShipments[position]
        shipment.reason = reason
        shipment.save()
    }
}