package com.goflash.dispatch.features.dispatch.presenter

import android.content.Context
import com.goflash.dispatch.features.dispatch.view.DispatchBagView

interface DispatchBagPresenter {

    fun onAttachView(context: Context, view : DispatchBagView)

    fun onDetachView()

    fun getBagCount()

    fun onBarcodeScanned(barcode : String)

    fun initiateDispatch(barcode: String)

    fun deleteData()

    fun getVehicleSealRequired()
}