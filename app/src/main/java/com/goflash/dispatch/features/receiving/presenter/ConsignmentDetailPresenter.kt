package com.goflash.dispatch.features.receiving.presenter

import android.content.Context
import com.goflash.dispatch.features.receiving.view.ConsignmentDetailView

interface ConsignmentDetailPresenter {

    fun onAttachView(context: Context , consignmentDetailView: ConsignmentDetailView)

    fun onDetachView()

    fun getBagShipments(tripId: Long)
}