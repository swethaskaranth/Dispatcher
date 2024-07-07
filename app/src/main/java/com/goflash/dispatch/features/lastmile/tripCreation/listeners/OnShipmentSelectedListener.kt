package com.goflash.dispatch.features.lastmile.tripCreation.listeners

import com.goflash.dispatch.data.UnassignedDTO

interface OnShipmentSelectedListener {

    fun onShipmentSelected(shipments : List<UnassignedDTO>)

    fun onShipmentUnselected(shipments : List<UnassignedDTO>)

    fun onViewAddress(position: Int, data: UnassignedDTO)

    fun onMpsCountClicked(shipmentId: String?,id: String?, count: Int)
}