package com.goflash.dispatch.features.rtoReceiving.presenter

import android.content.Context
import com.goflash.dispatch.features.rtoReceiving.view.ReceiveShipmentsListView

interface ReceiveShipmentListPresenter {

    fun onAttach(context: Context, view : ReceiveShipmentsListView)

    fun onDetach()
}