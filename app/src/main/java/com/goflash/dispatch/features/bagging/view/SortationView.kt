package com.goflash.dispatch.features.bagging.view

import com.goflash.dispatch.data.PackageDto
import com.goflash.dispatch.data.ScannedOrder

interface SortationView{

    fun onSuccessCancelledOrderScan(result: PackageDto)

    fun onFailure(error: Throwable?)

    fun showBinName(binNumber : String?, orderId : String?){}

    fun onSuccessBinScan()

    fun displayScannedOrders(order: ScannedOrder)

}