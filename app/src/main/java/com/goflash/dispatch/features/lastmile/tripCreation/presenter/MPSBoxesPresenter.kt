package com.goflash.dispatch.features.lastmile.tripCreation.presenter

import android.content.Context
import com.goflash.dispatch.features.lastmile.tripCreation.view.MPSBoxesView

interface MPSBoxesPresenter {

    fun onAttach(context: Context, view : MPSBoxesView)

    fun onDetach()

    fun fetchChildShipments(parentShipment:String)
}