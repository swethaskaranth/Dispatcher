package com.goflash.dispatch.data

import com.google.gson.annotations.SerializedName

data class CreateInwardRunItemRequest(
    val shipmentId: Int,
    val scanStatus: String,
    val exceptionReasons: List<String>? = null
)
