package com.goflash.dispatch.features.cash.view

import com.goflash.dispatch.data.BalancesDTO

interface CreateSummaryView {

    fun onBalanceFetched(balancesDTO: BalancesDTO)

    fun onFailure(error : Throwable?)

    fun setExpense(expenseCount: Int, amount : Long, totalAmount : Long,cashPickup : Long, cashClosing : Long)

    fun onCreateSuccess()

}