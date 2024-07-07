package com.goflash.dispatch.model

data class PincodeVerify(
    val pincode: String,
    val shipperCode: String? = null,
    val serviceable: Boolean,
    val assetName: String? = null,
    val assetId: String? = null

)