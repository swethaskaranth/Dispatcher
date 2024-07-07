package com.goflash.dispatch.data

data class CashBreakUp(val referenceId: String,
                       val amount : Int,
                       val name : String,
                       val transactionId: String? = null,
                       val paymentType: String? = null
)