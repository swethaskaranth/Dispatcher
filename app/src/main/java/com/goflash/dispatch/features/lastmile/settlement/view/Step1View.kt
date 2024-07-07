package com.goflash.dispatch.features.lastmile.settlement.view

import com.goflash.dispatch.data.UndeliveredShipmentDTO

interface Step1View {

    fun onSuccess(data: UndeliveredShipmentDTO)

    fun onFailure(error : Throwable?)

    fun showAlert(msg: String)

}