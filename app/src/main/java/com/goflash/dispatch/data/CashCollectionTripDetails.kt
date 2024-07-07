package com.goflash.dispatch.data

data class CashCollectionTripDetails(val tripId: Long,
                                     val sprinterName: String,
                                     val reconDateTime: String,
                                     val actualCashAmountCollected: Double)