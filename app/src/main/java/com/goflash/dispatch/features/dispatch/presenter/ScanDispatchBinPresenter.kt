package com.goflash.dispatch.features.dispatch.presenter

import android.content.Context
import com.goflash.dispatch.data.ScannedOrder
import com.goflash.dispatch.features.dispatch.view.ScanDispatchBinView

interface ScanDispatchBinPresenter{

    fun onAttachView(context: Context, view: ScanDispatchBinView)

    fun onDetachView()

    fun dispatchShipments(scannedOrders : List<ScannedOrder>)

    fun initiateDispatch(barcode: String)

    fun getPackage()


}