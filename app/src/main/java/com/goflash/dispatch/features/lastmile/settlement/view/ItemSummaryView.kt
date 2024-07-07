package com.goflash.dispatch.features.lastmile.settlement.view

import android.content.Intent
import com.goflash.dispatch.data.Item

interface ItemSummaryView {

    fun onItemsFetched(items : List<Item>)

    fun enableOrDisableProceed(disable : Boolean)

    fun goToReviewItemActivity(extras : Intent)

    fun goToScanShipmentActivity()

    fun setButtonToProceed()

    fun goToStep3Activity()

    fun startVerifyImageActivity()

    fun startReceiveFmPickupActivity()

    fun goToReceiveItemActivity(extras : Intent)

    fun onShowProgress()

    fun onImageUploaded(queryImageUrl: String, key : String)

    fun onFailure(error : Throwable?)

    fun startAckDeliverySlipReconActivity()

}