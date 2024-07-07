package com.goflash.dispatch.model

data class DeleteShipment(
    val shipmentId: String,
    val reason: String? = null,
    val referenceId: String? = null,
    val assetId: String? = null,
    val assetName: String? = null,
    var childShipment:List<ShipmentDTO> = mutableListOf()

)