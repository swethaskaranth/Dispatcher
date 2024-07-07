package com.goflash.dispatch.presenter.views

import com.goflash.dispatch.data.PackageDto

interface HomeView{

    fun takeToDispatchBinScreen(packageDto : PackageDto, isPackageDispatchable : Boolean)

    fun takeToCancelledScreen()

    fun takeToDispatchBagScreen()

    fun showBaggedCount(bags : Int,shipments : Int)

    fun showDispatchedCount(trips : Int)

    fun showReceivedCount(bags : Int,shipments : Int)

    fun onFailure(error : Throwable?)

    fun takeToDispatchScreen()

    fun takeToReceiveScanScreen(runId: Int)

    fun takeToReceivingListScreen(disableComplete: Boolean)

}