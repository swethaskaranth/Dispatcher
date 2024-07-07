package com.goflash.dispatch.features.bagging.presenter

import android.content.Context
import android.content.Intent
import com.goflash.dispatch.data.PackageDto
import com.goflash.dispatch.features.bagging.view.ScanSortBinView

interface ScanSotrBinPresenter{

    fun onAttachView(context: Context, view: ScanSortBinView)

    fun onDetachView()

    fun onBinScan(PackageDto: PackageDto)

    fun sendIntent(intent: Intent)

    fun onBarcodeScanned(barcode: String)


}