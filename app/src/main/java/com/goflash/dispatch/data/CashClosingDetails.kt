package com.goflash.dispatch.data

/**
 *Created by Ravi on 09/10/20.
 */
data class CashClosingDetails(
    val elements: Int,
    val pages: Int,
    val data: MutableList<CashDetails>

)