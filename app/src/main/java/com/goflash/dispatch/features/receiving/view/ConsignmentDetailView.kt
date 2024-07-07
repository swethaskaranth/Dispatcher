package com.goflash.dispatch.features.receiving.view

import com.goflash.dispatch.data.ShipmentCount


interface ConsignmentDetailView {

    fun onFailure(error : Throwable?)

    fun onSuccess(list : ArrayList<ShipmentCount>)
}