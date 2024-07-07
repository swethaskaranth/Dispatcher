package com.goflash.dispatch.features.rtoReceiving.presenter

import android.content.Context
import android.content.Intent
import com.goflash.dispatch.data.ReceivingShipmentDTO
import com.goflash.dispatch.features.rtoReceiving.view.ScanReceiveShipmentView

interface ScanReceiveShipmentPresenter {

    fun onAttach(context: Context, view: ScanReceiveShipmentView)

    fun onDetach()

    fun sendIntent(intent: Intent)

    fun getInwardRunItems()

    fun onBarcodeScanned(barcode: String)

    fun getExceptionReasons(position: Int)

    fun onReasonSelected(status: String, wayBillNumber: String, exceptions: List<String>)

    fun completeInwardRun()

    fun getMpsRunItems(position: Int)

    fun onSortationComplete(shipment: ReceivingShipmentDTO)
}