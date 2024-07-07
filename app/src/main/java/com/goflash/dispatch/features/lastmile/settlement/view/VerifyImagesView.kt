package com.goflash.dispatch.features.lastmile.settlement.view

import com.goflash.dispatch.data.AckForRecon
import com.goflash.dispatch.data.AckSlipDto

interface VerifyImagesView {

    fun onAckSlipsFetched(ackSlips: MutableList<AckForRecon>)

    fun enableOrDisableProceed(disable: Boolean)

    fun onShowProgress()

    fun onHideProgress()

    fun onAckSlipUploaded()

    fun onFailure(error: Throwable?)

    fun startReviewActivity(lbn: String)

    fun goToStep3Activity()

    fun startAckDeliverySlipReconActivity()
}