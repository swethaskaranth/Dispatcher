package com.goflash.dispatch.features.lastmile.settlement.view

import com.goflash.dispatch.data.AckSlipDto

interface ReviewImagesView {

    fun onAckSlipsFetched(slips: MutableList<AckSlipDto>)

    fun setApproveButton(count: Int)

    fun onImagesApproved(count: Int)

    fun onShowProgress()

    fun onHideProgress()

    fun onFailure(error: Throwable?)

    fun onAckSlipUploaded(ackSlipDto: AckSlipDto)

    fun startApproveActivity(position: Int, lbn: String)
}