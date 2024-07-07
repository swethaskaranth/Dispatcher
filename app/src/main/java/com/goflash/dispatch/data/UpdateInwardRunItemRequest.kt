package com.goflash.dispatch.data

import com.google.gson.annotations.SerializedName

data class UpdateInwardRunItemRequest(
    val shipmentId: Int,
    val exceptionReasons: List<String>,
    val scanStatus: String
)