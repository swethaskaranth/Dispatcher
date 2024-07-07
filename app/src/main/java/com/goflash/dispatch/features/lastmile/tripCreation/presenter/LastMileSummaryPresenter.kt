package com.goflash.dispatch.features.lastmile.tripCreation.presenter

import android.content.Context
import com.goflash.dispatch.data.ReceiveCdsCash
import com.goflash.dispatch.features.lastmile.tripCreation.view.LastMileSummaryView

interface LastMileSummaryPresenter {

    fun onAttachView(context: Context,summaryView: LastMileSummaryView)

    fun onDetachView()

    fun getCashInHand( receiveCdsCash: ReceiveCdsCash?)

    fun getTasksByTripId(tripId: String, receiveCdsCash: ReceiveCdsCash?)
}