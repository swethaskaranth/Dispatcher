package com.goflash.dispatch.features.lastmile.settlement.view

import com.goflash.dispatch.data.FmPickedShipment

interface ReceiveFmPickupView {

    fun onShipmentsFetched(list : List<FmPickedShipment>)

    fun setShipmentCount(scanned: Int, total: Int)

    fun onFailure(error : Throwable?)

    fun showAlreadyScanned(barcode: String)
}