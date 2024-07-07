package com.goflash.dispatch.features.rtoReceiving.view

interface ScanBinView {

    fun onSuccessBinScan()

    fun onFailure(error: Throwable?)
}