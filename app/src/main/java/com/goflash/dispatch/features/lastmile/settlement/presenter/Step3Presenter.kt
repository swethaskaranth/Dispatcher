package com.goflash.dispatch.features.lastmile.settlement.presenter

import android.content.Context
import com.goflash.dispatch.data.TripSettlementCompleteDTO
import com.goflash.dispatch.data.TripSettlementDTO
import com.goflash.dispatch.features.lastmile.settlement.view.Step3View
import java.time.temporal.TemporalAmount

interface Step3Presenter {

    fun onAttach(context: Context, view : Step3View)

    fun onDetach()

    fun getCashCollection(tripId: String): TripSettlementCompleteDTO

    fun settleTrip(tripId: String, cashAmount: String, chequeAmount: String)

}