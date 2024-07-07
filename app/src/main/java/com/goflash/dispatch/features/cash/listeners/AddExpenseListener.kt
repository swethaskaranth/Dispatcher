package com.goflash.dispatch.features.cash.listeners

interface AddExpenseListener {

    fun onUpload(position : Int)

    fun onAmountEntered()

    fun deleteExpense(position: Int)

    fun deleteImage(position: Int)
}