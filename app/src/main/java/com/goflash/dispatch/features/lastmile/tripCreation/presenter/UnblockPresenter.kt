package com.goflash.dispatch.features.lastmile.tripCreation.presenter

import android.content.Context
import com.goflash.dispatch.features.lastmile.tripCreation.view.UnblockView

interface UnblockPresenter{

    fun onAttachView(context: Context, view: UnblockView)

    fun onDetachView()

    fun unBlockShipement(shipmentId: String, referenceId: String)

}