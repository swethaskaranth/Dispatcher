package com.goflash.dispatch.features.lastmile.tripCreation.presenter

import android.content.Context
import com.goflash.dispatch.features.lastmile.tripCreation.view.CancelView
import com.goflash.dispatch.features.lastmile.tripCreation.view.ReasonView
import com.goflash.dispatch.model.ShipmentDTO

interface SelectReasonPresenter{

    fun onAttachView(context: Context, view: ReasonView)

    fun onDetachView()

    fun verifyPincode(pincode: String, shipmentId: String)

    fun cancelShipment(reason: String, shipmentId: String, list: List<ShipmentDTO>)

    fun updatePincode(pincode: String, shipmentId: String, assetId: String, assetName: String)

    fun getChildShipments(shipmentId: String)

}