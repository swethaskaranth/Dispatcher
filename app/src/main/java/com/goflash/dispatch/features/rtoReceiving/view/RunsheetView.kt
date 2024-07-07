package com.goflash.dispatch.features.rtoReceiving.view

import com.goflash.dispatch.data.InwardRun
import com.goflash.dispatch.data.InwardRunItem
import com.goflash.dispatch.data.PartnerNameDTO

interface RunsheetView {

    fun onInwardRunFetched(createdOn: String?, partnerName: String?, count: Int, items: List<InwardRunItem>)

    fun onFailure(error: Throwable?)

    fun onUrlFetched(url: String)

    fun onMpsRunItemsFetched(total: Int, items: List<InwardRunItem>)
}