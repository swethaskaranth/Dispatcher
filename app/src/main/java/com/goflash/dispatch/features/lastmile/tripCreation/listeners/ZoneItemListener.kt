package com.goflash.dispatch.features.lastmile.tripCreation.listeners

import com.goflash.dispatch.data.SprinterForZone
import com.goflash.dispatch.data.ZoneDetails
import com.goflash.dispatch.model.ZoneSprinterDTO

interface ZoneItemListener {

    fun addToMergeList(zone: ZoneSprinterDTO)

    fun removeFromMergeList(zone: ZoneSprinterDTO)

    fun onItemSelected(position : Int)

    fun deMergeZones(zone: ZoneSprinterDTO)

    fun removeSprinter(zoneId : Int,sprinter : SprinterForZone)

    fun onCancelTrip(zoneId : Int)
}