package com.goflash.dispatch.data

data class MidMileDispatchedRunsheet(
    val id: Long,
    val dispatchTime: String?,
    val runsheetUrl: String?,
    val shipmentCount: Int?,
    val assetId: String?,
    val tripId: Long
)
