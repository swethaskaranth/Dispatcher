package com.goflash.dispatch.features.receiving.view

import com.goflash.dispatch.data.VehicleDetails

/**
 *Created by Ravi on 2019-09-08.
 */
interface SummaryView {

    fun onFailure(error : Throwable?)

    fun onSuccess()

    fun onShowProgress()

    fun onHideProgress()

    fun updateViews(bagDetails: MutableList<VehicleDetails>)

    fun enableButton()

    fun takeToReceivingActivity()

    fun showErrorAndRedirect(message: String)
}