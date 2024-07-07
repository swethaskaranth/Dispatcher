package com.goflash.dispatch.features.lastmile.settlement.presenter

import android.content.Context
import com.goflash.dispatch.features.lastmile.settlement.view.SettlementScanShipmentView

interface SettlementScanShipmentPresenter {

    fun onAttachView(context: Context, view : SettlementScanShipmentView)

    fun onDetachView()

    fun setTripId(tripId: Long)

    fun onBarcodeScanned(barcode : String, tripId : Long)

    fun getShipments()

    fun onNext(tripId: Long)

}