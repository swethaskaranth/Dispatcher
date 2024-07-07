package com.goflash.dispatch.features.lastmile.settlement.presenter

import android.content.Context
import com.goflash.dispatch.data.TripSettlementDTO
import com.goflash.dispatch.data.UndeliveredShipmentDTO
import com.goflash.dispatch.features.lastmile.settlement.view.Step1View

interface Step1Presenter {

    fun onAttach(context: Context, view : Step1View)

    fun onDetach()

    fun onBarcodeScanned(barcode : String)

    fun getUndeliveredData(tripId: String): TripSettlementDTO

}