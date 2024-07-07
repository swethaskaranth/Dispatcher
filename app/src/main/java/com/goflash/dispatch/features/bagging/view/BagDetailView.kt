package com.goflash.dispatch.features.bagging.view

import com.goflash.dispatch.data.PackageDto

interface BagDetailView {

    fun showShipmentCount(size : Int?)

    fun onSuccess(message : String)

    fun restoreBagState()

    fun onFailure(error : Throwable?)

    fun goToShipmentsActivity(packageDto: PackageDto)

    fun setBagDestination(destination : String)

    fun setTripDetails(binNumber: String)

    fun goToHomeActivity()

}