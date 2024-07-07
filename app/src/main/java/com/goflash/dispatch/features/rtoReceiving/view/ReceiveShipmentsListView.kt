package com.goflash.dispatch.features.rtoReceiving.view

import com.goflash.dispatch.data.InwardRun

interface ReceiveShipmentsListView {

    fun onInwardRunsFetched(list: List<InwardRun>)

    fun onFailure(error: Throwable?)
}