package com.goflash.dispatch.features.bagging.view

import com.goflash.dispatch.data.PackageDto

interface ScannedShipmentsView {

    fun setBinNumnberAndCount(binNumber: String, count : Int)

    fun refereshList()

    fun showSnackBar(message : String)

    fun onFailure(error : Throwable?)

    fun sendUpdatedList(packageDto: PackageDto, bagCreated : Boolean)

    fun hideScanner()

    fun finishActivity()

}