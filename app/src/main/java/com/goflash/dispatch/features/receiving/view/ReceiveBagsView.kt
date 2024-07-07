package com.goflash.dispatch.features.receiving.view

import com.goflash.dispatch.data.ReceivingDto
import com.goflash.dispatch.data.VehicleDetails
import com.goflash.dispatch.model.BagDetails

/**
 *Created by Ravi on 2019-09-08.
 */
interface ReceiveBagsView {

    fun onFailure(error : Throwable?)

    fun onSuccess()

    fun onShowProgress()

    fun onHideProgress()

    fun onSetViews(receivingDto: ReceivingDto, items: MutableList<VehicleDetails>, bagDetails: MutableList<BagDetails>)

    fun checkForCompletion(items: MutableList<VehicleDetails>)

    fun showMessage(msg: String)

    fun setBagId(bagId: String, destination :String)
}