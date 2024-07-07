package com.goflash.dispatch.features.lastmile.settlement.view

import com.goflash.dispatch.data.Item

interface ReceiveItemView {

    fun onItemsFetched(items : List<Item>)

    fun enableOrDisableProceed(disable : Boolean)

    fun onFailure(error : Throwable?)

    fun goToScanShipmentActivity()

    fun setButtonToProceed()

    fun goToStep3Activity()

    fun startVerifyImageActivity()

    fun startReceiveFmPickupActivity()

    fun onShowProgress()

    fun onImageUploaded()

    fun startAckDeliverySlipReconActivity()

}