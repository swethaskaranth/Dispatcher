package com.goflash.dispatch.features.dispatch.presenter

import android.content.Context
import android.content.Intent
import com.goflash.dispatch.features.dispatch.view.DispatchVehicleView

interface DispatchVehiclePresenter {

    fun onAttachView(context: Context, view : DispatchVehicleView)

    fun onDetachView()

    fun sendIntent(intent: Intent)

    fun onBarcodeScanned(barcode : String)

    fun getInvoiceList()

    fun getInvoiceUrl()

    fun onPrintFinished()

    fun getConsolidatedManifest() }