package com.goflash.dispatch.data

data class BalancesDTO(var cashClosingSyncId: String?,
                       var openingBalance: Long?,
                       var totalAmount: Long?,
                       var totalCashCollected: Long?,
                       var lastTripId: Int?)