package com.goflash.dispatch.features.cash.presenter

import android.content.Context
import com.goflash.dispatch.features.cash.view.CashCollectionBreakupView

interface CashCollectionBreakupPresenter {

    fun onAttach(context: Context, view : CashCollectionBreakupView)

    fun onDetach()

    fun getCashCollectionBreakup(cashClosingId: String?, size: Int, adhoc: Boolean)

    fun isLastPage(): Boolean
}