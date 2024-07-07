package com.goflash.dispatch.features.lastmile.tripCreation.presenter

import android.content.Context
import com.goflash.dispatch.features.lastmile.tripCreation.view.ScanShipmentView

interface ScanShipmentPresenter {

    fun onAttach(context: Context, view : ScanShipmentView)

    fun onDetach()

    fun getShipmentsForTrip(tripId : Long)

    fun addShipment(barcode : String,tripId : Long)

    fun removeShipment(barcode : String,tripId : Long)

    fun deleteTrip(tripId : Long)

    fun getAddressDetails(position: Int, shipmentId: String)
}