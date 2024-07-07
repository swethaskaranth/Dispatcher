package com.goflash.dispatch.data

/**
 *Created by Ravi on 09/10/20.
 */
data class CashDetails(
    val id: Int,
    val createdOn :String,
    val updatedOn :String,
    val entityId  :String,
    val entityType :String,
    val openingBalance :Int,
    val closingBalance :Int,
    val cashCollected :Int,
    val cashDeposit   :Int,
    val cashDepositReceiptNumber :String,
    val cashDepositReceiptUrl :String,
    val expenses :Int,
    val denominationId :Int,
    val cashClosingTimestamp :String,
    val denomination :String,
    val ledgerEntry :String
)