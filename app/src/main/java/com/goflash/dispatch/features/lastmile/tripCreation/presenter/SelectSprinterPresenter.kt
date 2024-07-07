package com.goflash.dispatch.features.lastmile.tripCreation.presenter

import android.content.Context
import com.goflash.dispatch.data.SprinterList
import com.goflash.dispatch.features.lastmile.tripCreation.view.SelectSprinterView

interface SelectSprinterPresenter {

    fun onAttachView(context: Context, view : SelectSprinterView)

    fun onDetachView()

    fun createTrip(sprinter : SprinterList)

    fun assignSprinter(tripId : Long, sprinter: SprinterList)

}