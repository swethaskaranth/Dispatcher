package com.goflash.dispatch.data

data class CashCollectionBreakupResponse(val elements: Int,
                                         val pages: Int,
                                         val hasNext: Boolean,
                                         val hasPrevious: Boolean,
                                         val content: List<CashCollectionTripDetails>
)
