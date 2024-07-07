package com.goflash.dispatch.features.lastmile.tripCreation.presenter.impl

import android.content.Context
import com.goflash.dispatch.features.lastmile.tripCreation.view.ScanShipmentView
import com.goflash.dispatch.features.lastmile.tripCreation.view.SummaryDetailView

interface SummaryDetailPresenter {

    fun onAttach(context: Context, view : SummaryDetailView)

    fun onDetach()

    fun getAddressDetails(position: Int, shipmentId: String)
}