package com.goflash.dispatch.features.lastmile.tripCreation.presenter

import android.content.Context
import com.goflash.dispatch.features.lastmile.tripCreation.view.ScanToSearchView

interface ScanToSearchPresenter {

    fun onAttach(context: Context, view : ScanToSearchView)

    fun onDetach()

    fun onBarcodeScanned(barcode : String)

    fun getAddressDetails(position: Int, shipmentId: String)
}