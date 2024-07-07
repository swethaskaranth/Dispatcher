package com.goflash.dispatch.features.lastmile.tripCreation.presenter

import android.content.Context
import com.goflash.dispatch.data.InTransitTrip
import com.goflash.dispatch.features.lastmile.tripCreation.view.UnassignedView

interface UnassignedShipmentPresenter {

    fun onAttach(context: Context, view : UnassignedView)

    fun onDetach()

    fun getShipments(type: String?, eddStart: String?, eddEnd: String?, referenceId: String?,tag: String?,excludedTrips: List<String>,serviceType: String?, paymentStatus: String?)

    fun getInTransitTrips() : ArrayList<InTransitTrip>

    fun getInTransitTripCount()

    fun setInTransitTrips(list: MutableList<InTransitTrip>)

    fun getAddressDetails(position: Int, shipmentId: String)
}