package com.goflash.dispatch.features.lastmile.settlement.view

import com.goflash.dispatch.data.ReturnShipmentDTO

interface SettlementScanShipmentView {

    fun onShipmentsFetched(list : List<String>)

    fun setShipmentCount(scanned : Int, total : Int)

    fun onFailure(error : Throwable?)

    fun takeToScanActivity(shipmentId : String, refId : String, partialDelivery : Boolean)

    fun takeToReviewActivity(shipmentId : String, refId : String, partialDelivery : Boolean)

    fun takeToItemSummaryActivity(shipmentId : String, refId : String, partialDelivery : Boolean)

    fun enableOrDisableProceed(enable : Boolean)

    fun startStep3CashActivity()

    fun startReceiveFmPickupActivity()

    fun startVerifyImageActivity()

    fun startAckDeliverySlipReconActivity()

}