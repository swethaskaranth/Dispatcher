package com.goflash.dispatch.presenter.views

interface CancelledRowView{

    fun setOrderId(orderId : String)

    fun setBackgroundColor(color : String)

    fun checkOrUncheck(visible : Boolean)

    fun setLBN(lbn : String)
}