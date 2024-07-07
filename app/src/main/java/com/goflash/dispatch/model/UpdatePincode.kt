package com.goflash.dispatch.model

data class UpdatePincode(
    val shipmentId: String,
    val pincode: String? = null,
    val referenceId: String? = null,
    val assetId: String? = null,
    val assetName: String? = null

)