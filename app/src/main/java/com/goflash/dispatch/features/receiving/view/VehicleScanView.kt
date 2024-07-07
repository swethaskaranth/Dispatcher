package com.goflash.dispatch.features.receiving.view

import com.goflash.dispatch.data.VehicleDetails

/**
 *Created by Ravi on 2019-09-08.
 */
interface VehicleScanView {

    fun onFailure(error : Throwable?)

    fun onSuccess(bagDetails: MutableList<VehicleDetails>)

    fun onShowProgress()

    fun onHideProgress()
}