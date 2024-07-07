package com.goflash.dispatch.features.rtoReceiving.presenter

import android.content.Context
import android.content.Intent
import com.goflash.dispatch.features.rtoReceiving.view.ReceiveMpsShipmentListView

interface ReceiveMpsShipmentListPresenter {

    fun onAttach(context: Context, view: ReceiveMpsShipmentListView)

    fun onDetach()

    fun sendIntent(intent: Intent)

    fun getExceptionReasons(position: Int)

    fun onReasonSelected(status: String, wayBillNumber: String, exceptions: List<String>)
}