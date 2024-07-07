package com.goflash.dispatch.features.lastmile.settlement.presenter

import android.content.Context
import com.goflash.dispatch.features.lastmile.settlement.view.FmPickupReviewView

interface FmPickupReviewPresenter {

    fun onAttachView(context: Context, view : FmPickupReviewView)

    fun onDetachView()

    fun getShipments(originName: String)

    fun onReasonSelected(position: Int, reason: String)
}