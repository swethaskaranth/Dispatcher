package com.goflash.dispatch.features.lastmile.settlement.presenter.impl

import android.content.Context
import co.uk.rushorm.core.RushSearch
import com.goflash.dispatch.data.TripSettlementDTO
import com.goflash.dispatch.data.UndeliveredShipmentDTO
import com.goflash.dispatch.di.interactor.SortationApiInteractor
import com.goflash.dispatch.features.lastmile.settlement.presenter.Step1Presenter
import com.goflash.dispatch.features.lastmile.settlement.view.Step1View
import rx.subscriptions.CompositeSubscription

class Step1PresenterImpl(private val sortationApiInteractor: SortationApiInteractor): Step1Presenter {

    private var mView : Step1View? = null
    private var compositeSubscription : CompositeSubscription? = null

    override fun onAttach(context: Context, view: Step1View) {
        this.mView = view
        compositeSubscription = CompositeSubscription()
    }

    override fun onDetach() {

        if(mView == null)
            return
        mView = null
        compositeSubscription?.unsubscribe()
        compositeSubscription = null
    }

    override fun onBarcodeScanned(barcode: String) {
        val data = RushSearch()
                .whereEqual("referenceId", barcode).or()
                .whereEqual("packageId", barcode).or()
                .whereEqual("lbn", barcode)
                .find(UndeliveredShipmentDTO::class.java)

        if(!data.isNullOrEmpty()){
            if (data.all { it.isScanned }) {
                mView?.showAlert("Barcode $barcode already scanned")
                return
            } else{
                val shipment = data.find { !it.isScanned }
                shipment!!.isScanned = true
                shipment.shipmentStatus = shipment.status
                shipment.status = "Completed"
                shipment.updated = System.currentTimeMillis()
                shipment.save()
                mView?.onSuccess(shipment)
            }
        }else
            mView?.showAlert("Wrong barcode $barcode scanned")
    }

    override fun getUndeliveredData(tripId: String): TripSettlementDTO {
        return RushSearch().whereEqual("tripId", tripId).findSingle(TripSettlementDTO::class.java)
    }
}