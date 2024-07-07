package com.goflash.dispatch.features.lastmile.tripCreation.listeners

import com.goflash.dispatch.data.UnassignedDTO

interface ShipmentSelectedListener {

    fun onShipmentSelected(position : Int)

    fun onDeleteShipemnt(position : Int, data: UnassignedDTO)

    fun onEditShipment(position : Int, data: UnassignedDTO)

    fun onViewDetails(position : Int, data: UnassignedDTO)
}