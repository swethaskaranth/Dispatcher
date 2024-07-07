package com.goflash.dispatch.data

data class MergeCreatedTripRequest(
    val fromTripId: String,
    val toTripId: String
)