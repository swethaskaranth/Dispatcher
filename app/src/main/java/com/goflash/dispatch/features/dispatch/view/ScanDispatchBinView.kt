package com.goflash.dispatch.features.dispatch.view

import com.goflash.dispatch.data.PackageDto

interface ScanDispatchBinView{

    fun onSuccess(message : String)

    fun dispatchCancelledOrdersPresent()

    fun onFailure(error : Throwable?)

    fun onSuccess(isDispatchable : Boolean,singleOrderscanned : Boolean)

    fun onPackageFetched(packageDto: PackageDto)
}