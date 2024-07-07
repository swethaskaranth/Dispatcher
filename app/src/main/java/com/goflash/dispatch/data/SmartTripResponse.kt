package com.goflash.dispatch.data

data class SmartTripResponse(val id : Int?,
                             val status : String?,
                             val assetId : Int?,
                             val zoneIds: List<Int>
)