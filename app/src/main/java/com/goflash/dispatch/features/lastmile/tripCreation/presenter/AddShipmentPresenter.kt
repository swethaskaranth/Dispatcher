package com.goflash.dispatch.features.lastmile.tripCreation.presenter

import android.content.Context
import com.goflash.dispatch.data.InTransitTrip
import com.goflash.dispatch.features.lastmile.tripCreation.view.AddShipmentView
import com.goflash.dispatch.model.CommonRequest

interface AddShipmentPresenter {

    fun onAttach(context: Context, view : AddShipmentView)

    fun onDetach()

    fun getShipments(type: String?, eddStart: String?, eddEnd: String?,tag : String?,excludedTrips: List<String>,serviceType: String?, paymentStatus: String?)

    fun addShipments(shipments : MutableList<CommonRequest>)

    fun getInTransitTrips() : ArrayList<InTransitTrip>

    fun getInTransitTripCount()

    fun setInTransitTrips(list: MutableList<InTransitTrip>)

    fun getAddressDetails(position: Int, shipmentId: String)

}