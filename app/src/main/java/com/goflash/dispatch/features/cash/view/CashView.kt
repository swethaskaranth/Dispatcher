package com.goflash.dispatch.features.cash.view

import com.goflash.dispatch.data.CashClosingDetails

/**
 *Created by Ravi on 01/10/20.
 */
interface CashView {

    fun onSuccess(cashClosingDetails: CashClosingDetails)

    fun onFailure(error : Throwable?)

}