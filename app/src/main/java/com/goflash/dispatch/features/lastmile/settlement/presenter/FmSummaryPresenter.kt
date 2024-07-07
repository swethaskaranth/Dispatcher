package com.goflash.dispatch.features.lastmile.settlement.presenter

import android.content.Context
import com.goflash.dispatch.features.lastmile.settlement.view.FmSummaryView

interface FmSummaryPresenter {

    fun onAttachView(context: Context, view : FmSummaryView)

    fun onDetachView()

    fun getShipments(tripId: Long)

    fun onNext(tripId: Long)

}