package com.goflash.dispatch.features.bagging.view

import com.goflash.dispatch.data.PackageDto

interface ScanSortBinView{

    fun onSuccess()

    fun onFailure(error : Throwable?)

    fun showBinName(binNumber : String?, orderId : String?)

    fun hideOrderLayout()

    fun takeToBagDetailScreen(packageDto: PackageDto)

}