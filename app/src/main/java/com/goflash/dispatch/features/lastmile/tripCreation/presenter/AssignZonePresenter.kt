package com.goflash.dispatch.features.lastmile.tripCreation.presenter

import android.content.Context
import com.goflash.dispatch.data.InTransitTrip
import com.goflash.dispatch.data.SprinterForZone
import com.goflash.dispatch.data.ZoneDetails
import com.goflash.dispatch.features.lastmile.tripCreation.view.AssignZoneView
import com.goflash.dispatch.model.ZoneSprinterDTO

interface AssignZonePresenter {

    fun onAttachView(context: Context, view : AssignZoneView)

    fun onDetachView()

    fun getShipments(type: String?, eddStart: String?, eddEnd: String?,excludedTrips: List<String>,tag : String?,serviceType: String?, paymentStatus: String?)

    fun mergeZones(list : MutableList<ZoneSprinterDTO>)

    fun deMergeZone(zoneId : Int)

    fun setSprinterForZone(zoneId : Int , sprinters: List<SprinterForZone>)

    fun onItemSelected(zoneId : Int)

    fun removeSprinterForZone(zoneId: Int, sprinter : SprinterForZone)

    fun applyZoneFilter(s : String)

    fun clearFilter()

    fun createTrips()

    fun clearSprinters()

    fun getInTransitTrips() : ArrayList<InTransitTrip>

    fun getInTransitTripCount()

    fun setInTransitTrips(list: MutableList<InTransitTrip>)

    fun cancelTripForZone(tripProcessId: Int)
}