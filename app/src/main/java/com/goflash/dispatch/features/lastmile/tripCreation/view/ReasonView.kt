package com.goflash.dispatch.features.lastmile.tripCreation.view

import com.goflash.dispatch.model.PincodeVerify
import com.goflash.dispatch.model.ShipmentDTO

/**
 *Created by Ravi on 2020-06-16.
 */
interface ReasonView {

    fun onSuccess(pincodeVerify: PincodeVerify)

    fun onFailure(error : Throwable?)

    fun onSubmitSuccess()

    fun onChildShipmentsFetched(shipments: MutableList<ShipmentDTO>)


}