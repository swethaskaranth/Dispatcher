package com.goflash.dispatch.features.lastmile.tripCreation.presenter

import android.content.Context
import com.goflash.dispatch.features.lastmile.tripCreation.view.OfdView

interface OfdPresenter{

    fun onAttachView(context: Context, view: OfdView)

    fun onDetachView()

    fun getOfdTrips()

}