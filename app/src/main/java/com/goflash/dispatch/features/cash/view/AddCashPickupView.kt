package com.goflash.dispatch.features.cash.view

import com.goflash.dispatch.data.CashPickupDTO

interface AddCashPickupView {

    fun onFailure(error: Throwable?)

    fun setData(cashPickupDTO: CashPickupDTO?)

    fun setEditTextData(cashPickup : CashPickupDTO?)

    fun finishActivity()

    fun onShowProgress()

    fun onHideProgress()

    fun setFileUrl(url : String, type : String)
}