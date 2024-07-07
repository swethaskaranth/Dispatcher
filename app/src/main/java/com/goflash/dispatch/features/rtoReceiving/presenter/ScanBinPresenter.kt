package com.goflash.dispatch.features.rtoReceiving.presenter

import android.content.Context
import com.goflash.dispatch.data.PackageDto
import com.goflash.dispatch.features.rtoReceiving.view.ScanBinView

interface ScanBinPresenter {

    fun onAttach(context: Context, view: ScanBinView)

    fun onDetach()

    fun onBinScan(barcode: String, packageDto: PackageDto)
}