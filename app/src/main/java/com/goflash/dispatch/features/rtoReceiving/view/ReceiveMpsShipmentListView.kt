package com.goflash.dispatch.features.rtoReceiving.view

import com.goflash.dispatch.data.InwardRunItem

interface ReceiveMpsShipmentListView {

    fun onFailure(error: Throwable?)

    fun setupData(count: Int, list: List<InwardRunItem>)

    fun onExceptionsFetched(waybillNumber: String, status: String?, list: List<String>)

    fun onStatusUpdated(runItem: InwardRunItem)
}