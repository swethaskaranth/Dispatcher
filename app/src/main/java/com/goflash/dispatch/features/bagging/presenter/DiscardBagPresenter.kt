package com.goflash.dispatch.features.bagging.presenter

import android.content.Context
import android.content.Intent
import com.goflash.dispatch.features.bagging.view.DiscardBagView

interface DiscardBagPresenter {

    fun onAttachView(context: Context, view : DiscardBagView)

    fun onDetachView()

    fun sendIntent(intent: Intent)

    fun onBarcodeScanned(barcode : String)

    fun discardBag()

    fun getShipmentList()
}