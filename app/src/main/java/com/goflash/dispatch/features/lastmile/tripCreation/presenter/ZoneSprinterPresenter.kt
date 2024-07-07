package com.goflash.dispatch.features.lastmile.tripCreation.presenter

import android.content.Context
import com.goflash.dispatch.data.SprinterForZone
import com.goflash.dispatch.features.lastmile.tripCreation.view.ZoneSprinterView

interface ZoneSprinterPresenter {

    fun onAttachView(context: Context, zoneSprinterView: ZoneSprinterView)

    fun onDetachView()

    fun getSprinters(selectedSprinter : List<SprinterForZone>)

}