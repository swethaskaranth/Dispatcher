package com.goflash.dispatch.features.lastmile.tripCreation.presenter

import android.content.Context
import com.goflash.dispatch.features.lastmile.tripCreation.view.CancelView

interface CancelPresenter{

    fun onAttachView(context: Context, view: CancelView)

    fun onDetachView()

    fun cancelShipement(reason: String, shipmentId: String)

}