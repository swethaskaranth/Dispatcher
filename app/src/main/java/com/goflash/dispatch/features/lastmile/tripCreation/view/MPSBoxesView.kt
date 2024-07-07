package com.goflash.dispatch.features.lastmile.tripCreation.view

import com.goflash.dispatch.data.ChildShipmentDTO
import com.goflash.dispatch.model.ShipmentDTO


interface MPSBoxesView {

    fun onFailure(error : Throwable?)

    fun onChildShipmentsFetched(shipments : List<ChildShipmentDTO>)
}