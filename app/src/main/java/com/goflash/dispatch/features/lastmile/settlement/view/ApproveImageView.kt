package com.goflash.dispatch.features.lastmile.settlement.view

import com.goflash.dispatch.data.AckSlipDto

interface ApproveImageView {

    fun onAckSlipsFetched(slips: MutableList<AckSlipDto>)

    fun onImageApproved(message: String)

    fun closeActivity()
}