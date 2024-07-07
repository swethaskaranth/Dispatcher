package com.goflash.dispatch.model

data class ActiveTrips(
    val status: String,
    val startDate: String? = null,
    val endDate: String? = null,
    val type: String? = null,
    val referenceId: String? = null,
    val eddStartDate: String? = null,
    val eddEndDate: String? = null,
    val tag: String? = null,
    val agentName: String? = null,
    val excludedTrips:List<String>,
    val assign: String = "assign",
    val routeId: String? = null
)