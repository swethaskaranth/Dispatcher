package com.goflash.dispatch.features.dispatch.view

interface InvoiceRowView {

    fun setInvoiceId(invoiceId : String?)

    fun setTime(time : String)

    fun setOnClickListeners()

    fun hideEwayBill()
}