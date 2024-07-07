package com.goflash.dispatch.features.bagging.view

import com.goflash.dispatch.data.BagDTO

interface DiscardBagView {

    fun onSuccess(message : String)

    fun onFailure(error : Throwable?)

    fun setShipmentCount(count : String)

    fun enableDiscardBtn()

    fun goToShipmentsActivity(bagDto: BagDTO)
}