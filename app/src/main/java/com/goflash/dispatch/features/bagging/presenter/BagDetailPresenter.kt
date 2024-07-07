package com.goflash.dispatch.features.bagging.presenter

import android.content.Context
import android.content.Intent
import com.goflash.dispatch.features.bagging.view.BagDetailView

interface BagDetailPresenter {

    fun onAttachView(context: Context, view: BagDetailView)

    fun onDetachView()

    fun sendIntent(intent: Intent)

    fun getShipmentCount()

    fun onBarcodeScanned(barcode : String)

    fun getShipmentList()

    fun onProceedClicked()
    fun shouldShowAlert(): Boolean
}