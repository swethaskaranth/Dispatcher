package com.goflash.dispatch.features.lastmile.settlement.presenter

import android.content.Context
import com.goflash.dispatch.features.lastmile.settlement.view.ReceiveFmPickupView

interface ReceiveFmPickupPresenter {

    fun onAttachView(context: Context, view : ReceiveFmPickupView)

    fun onDetachView()

    fun setTripId(tripId: Long)

    fun getShipments()

    fun onBarcodeScanned(barcode: String)
}