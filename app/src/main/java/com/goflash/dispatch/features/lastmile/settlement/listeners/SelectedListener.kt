package com.goflash.dispatch.features.lastmile.settlement.listeners

import com.goflash.dispatch.data.UndeliveredShipmentDTO

interface SelectedListener {

    fun onShipmentSelected(position : Int, data: UndeliveredShipmentDTO){}

    fun onFmShipmentReasonSelected(position: Int, reason: String){}

}