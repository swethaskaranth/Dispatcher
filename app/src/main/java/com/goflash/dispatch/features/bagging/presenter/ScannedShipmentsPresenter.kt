package com.goflash.dispatch.features.bagging.presenter

import android.content.Context
import android.content.Intent
import com.goflash.dispatch.presenter.views.CancelledRowView
import com.goflash.dispatch.features.bagging.view.ScannedShipmentsView

interface ScannedShipmentsPresenter {

    fun onAttachView(context: Context, view : ScannedShipmentsView)

    fun onDetachView()

    fun sendIntent(intent : Intent)

    fun getCount() : Int

    fun onBindCanceeldRowView(position : Int,holder :CancelledRowView)

    fun onBarcodeScanned(barcode : String)

    fun undoRemove()

    fun getUpdatedShipmentList()
}