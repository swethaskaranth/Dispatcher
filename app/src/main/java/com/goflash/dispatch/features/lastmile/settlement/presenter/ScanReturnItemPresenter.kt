package com.goflash.dispatch.features.lastmile.settlement.presenter

import android.content.Context
import android.content.Intent
import com.goflash.dispatch.features.lastmile.settlement.view.ScanReturnItemView
import com.goflash.dispatch.type.ReconStatus

interface ScanReturnItemPresenter {

    fun onAttachView(context: Context, view : ScanReturnItemView)

    fun onDetachView()

    fun setShipmentId(id: String, partialDelivery: Boolean)

    fun getItems()

    fun onBarcodeScanned(barcode : String)

    fun setAcceptReject(position : Int, reconStatus: ReconStatus, reason : String, rejectRemarks: String)

}