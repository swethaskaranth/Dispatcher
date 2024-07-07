package com.goflash.dispatch.features.dispatch.view



interface InvoiceListView{

    fun showProgressBar()

    fun hideProgressBar()

    fun onPrintUrlFetched(message : String, result: String)

    fun onFailure(error: Throwable?)

    fun setDestinationSpinner(list : ArrayList<String>)

    fun setDateSpinner(list : MutableList<String>)

    fun refreshList(count : Int)
}