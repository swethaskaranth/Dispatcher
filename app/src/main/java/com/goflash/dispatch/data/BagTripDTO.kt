package com.goflash.dispatch.data

data class BagTripDTO(
    val tripId: String?,
    val vehicleSealId: String?,
    val sprinterDetailsDto: Sprinter,
    val bagAdded: MutableList<BagDTO>,
    val transportMode: String?,
    val invoiceRequired: Boolean? = null
)