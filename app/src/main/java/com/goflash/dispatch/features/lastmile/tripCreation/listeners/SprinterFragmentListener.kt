package com.goflash.dispatch.features.lastmile.tripCreation.listeners

import com.goflash.dispatch.data.SprinterForZone
import com.goflash.dispatch.data.ZoneDetails

interface SprinterFragmentListener {

    fun removeFragment()

    fun setSprinterData(zone : Int , sprinters : List<SprinterForZone>)

}