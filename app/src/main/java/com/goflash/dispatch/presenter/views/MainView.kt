package com.goflash.dispatch.presenter.views

import com.goflash.dispatch.data.PackageDto

interface MainView{

    fun takeToDispatchBinSCreen(packageDto : PackageDto, isPackageDispatchable : Boolean)

    fun takeToCancelledScreen()

    fun onLogoutSuccesful()

    fun onFailure(error : Throwable?)

}