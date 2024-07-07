package com.goflash.dispatch.features.lastmile.tripCreation.view

import com.goflash.dispatch.data.SprinterForZone
import com.goflash.dispatch.data.SprinterList

interface ZoneSprinterView {

    fun onSprintersFetched(list : List<SprinterForZone>)

    fun onFailure(error : Throwable?)


}