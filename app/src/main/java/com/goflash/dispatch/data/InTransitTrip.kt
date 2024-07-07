package com.goflash.dispatch.data

import java.io.Serializable

data class InTransitTrip(
    val id: Long,
    val status: String,
    val agentId: String,
    val agentName: String,
    val agentPhone: String,
    val createdOn: String,
    val updatedOn: String,
    val vehicleId: String,
    val assetName: String,
    val shipmentCount: Int,
    val asset: String,
    var selected: Boolean = true
): Serializable