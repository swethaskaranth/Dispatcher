package com.goflash.dispatch.features.cash.presenter

import android.content.Context
import com.goflash.dispatch.features.cash.view.CashView

/**
 *Created by Ravi on 01/10/20.
 */
interface CashPresenter {

    fun onAttach(context: Context, view : CashView)

    fun onDeAttach()

    fun getAllCash(page:Int, size: Int)
}