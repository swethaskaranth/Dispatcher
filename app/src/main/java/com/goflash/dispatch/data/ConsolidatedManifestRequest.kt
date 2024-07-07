package com.goflash.dispatch.data

data class ConsolidatedManifestRequest(
    val startDate: String? = null,
    val endDate: String?= null,
    val tripId: Long?= null
)
