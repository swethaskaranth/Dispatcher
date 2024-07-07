package com.goflash.dispatch.presenter.views

interface CancelledView{

    fun onFailure(error : Throwable?)

    fun showScanBin(binNumber : String?)

    fun refreshList()

    fun showOrHideScanLabel(singleItem : Boolean, status : String?,binNumber : String?)

    fun finishSortationTask()

    fun takeToMainActivity()

    fun takeToScanActivity()
}