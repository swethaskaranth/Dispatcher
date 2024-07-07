package com.goflash.dispatch.features.lastmile.settlement.view

import com.goflash.dispatch.data.FmPickedShipment

interface FmSummaryView {

    fun onShipmentsFetched(map: Map<String,List<FmPickedShipment>>)

    fun enableOrDisableProceed(enable: Boolean)

    fun startStep3CashActivity()

    fun startVerifyImageActivity()

    fun startAckDeliverySlipReconActivity()

}