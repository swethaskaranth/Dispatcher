package com.goflash.dispatch.features.cash.view

import android.content.Intent
import com.goflash.dispatch.data.ExpenseDTO

interface AddExpenseView {

    fun onFailure(error : Throwable?)

    fun onExpenseFetched(expenses : MutableList<ExpenseDTO>)

    fun onShowProgress()

    fun onHideProgress()

    fun setTotalExpense(expenseCount: Int,expense : Long, enable : Boolean)

    fun finishActivity()


}