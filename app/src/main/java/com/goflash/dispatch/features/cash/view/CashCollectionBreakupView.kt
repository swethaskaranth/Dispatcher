package com.goflash.dispatch.features.cash.view

import com.goflash.dispatch.data.AdhocCashCollectionBreakup
import com.goflash.dispatch.data.CashCollectionTripDetails

interface CashCollectionBreakupView {

    fun onFailure(error: Throwable?)

    fun onBreakupFetched(list: MutableList<CashCollectionTripDetails>)

    fun showNoElementsView()

    fun onAdhocBreakupFetched(list: MutableList<AdhocCashCollectionBreakup>)
}