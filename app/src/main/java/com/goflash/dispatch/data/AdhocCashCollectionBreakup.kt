package com.goflash.dispatch.data

data class AdhocCashCollectionBreakup(
    val transactionId: Int,
    val depositor: String,
    val amount: Double,
    val createdOn: String
)
