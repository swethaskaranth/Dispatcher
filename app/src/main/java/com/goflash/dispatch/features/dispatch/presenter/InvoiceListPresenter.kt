package com.goflash.dispatch.features.dispatch.presenter

import android.content.Context
import com.goflash.dispatch.features.dispatch.view.InvoiceListView
import com.goflash.dispatch.features.dispatch.view.InvoiceRowView

interface InvoiceListPresenter{

    fun onAttachView(context: Context, view: InvoiceListView)

    fun onDetachView()

    fun getInvoiceList()

    fun omInvoiceDateRangeChanged(position : Int)

    fun onVehicelSelected(position: Int)

    fun searchByInvoiceNumber(str : String)

    fun clearFilter()


}