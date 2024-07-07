package com.goflash.dispatch.features.dispatch.ui.interfaces

import com.goflash.dispatch.features.dispatch.view.InvoiceRowView

interface InvoiceListController {

    fun getCount() : Int

    fun onBindInvoiceRowView(position : Int, invoiceRowView: InvoiceRowView)

    fun onPrintInvoiceClicked(position : Int)

    fun onEwayBillClicked(position : Int)
}