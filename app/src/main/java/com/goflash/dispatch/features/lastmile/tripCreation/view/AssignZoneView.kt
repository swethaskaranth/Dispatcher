package com.goflash.dispatch.features.lastmile.tripCreation.view

import com.goflash.dispatch.data.SprinterForZone
import com.goflash.dispatch.model.ZoneSprinterDTO

interface AssignZoneView {

    fun displayProgress()

    fun onFailure(error : Throwable?)

    fun onShipmentsFetched(shipments : MutableList<ZoneSprinterDTO>)

    fun startSprinterActivity(zoneId: Int, sprinters : MutableList<SprinterForZone>, minSprinterCount: Int)

    fun setFlag(b : Boolean)

    fun onCreateSuccess()

    fun  showIntransitCount(count: Int)

    fun onCancelSuccess()

}