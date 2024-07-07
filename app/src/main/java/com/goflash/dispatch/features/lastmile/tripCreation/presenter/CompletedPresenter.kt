package com.goflash.dispatch.features.lastmile.tripCreation.presenter

import android.content.Context
import com.goflash.dispatch.data.TripSettlementDTO
import com.goflash.dispatch.features.lastmile.tripCreation.view.CompletedView

interface CompletedPresenter{

    fun onAttachView(context: Context, view: CompletedView)

    fun onDetachView()

    fun getCompletedTrips()

    fun getReconStartedTrip(tripId: Long)

    fun saveTripSettlementData()

    fun getUndeliveredData(tripId: String): TripSettlementDTO

}