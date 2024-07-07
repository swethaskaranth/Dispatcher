package com.goflash.dispatch.features.lastmile.tripCreation.listeners

interface InTransitTripListener {

    fun onSelectOrDeselectAll(select: Boolean)

    fun onSelectOrDeselectItem(select: Boolean, position: Int)
}