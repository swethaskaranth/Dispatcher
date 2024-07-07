package com.goflash.dispatch.features.dispatch.view

import com.goflash.dispatch.data.ScannedOrder

interface OrderListView{

    //fun onOrdersFetched(scannedOrders : MutableList<ScannedOrder>)

    fun onFailure(message : String)

    fun showOrderCount(binNumber : String?)

}