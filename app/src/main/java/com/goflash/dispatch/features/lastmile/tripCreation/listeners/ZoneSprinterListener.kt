package com.goflash.dispatch.features.lastmile.tripCreation.listeners

import com.goflash.dispatch.data.SprinterForZone

interface ZoneSprinterListener {

    fun addSprinterToList(sprinter : SprinterForZone)

    fun removeSprinterFromList(sprinter: SprinterForZone)
}