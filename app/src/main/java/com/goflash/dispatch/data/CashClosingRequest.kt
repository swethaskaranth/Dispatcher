package com.goflash.dispatch.data

data class CashClosingRequest(
    var expensesList: List<ExpenseDTO>,
    var cashDepositFileUrl: String?,
    var cashDepositReceiptNumber: String,
    var openingBalance: Long,
    var closingBalance: Long,
    var cashDeposit: Long,
    var cashCollected: Long,
    var expenses: Long,
    var one: Long,
    var two: Long,
    var five: Long,
    var ten: Long,
    var twenty: Long,
    var fifty: Long,
    var hundred: Long,
    var twoHundred: Long,
    var fiveHundred: Long,
    var twoThousand: Long,
    var lastTripId: Int,
    var depositType: String?,
    var atmId: String?
)