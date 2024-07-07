package com.goflash.dispatch.features.lastmile.tripCreation.view

import com.goflash.dispatch.data.UnassignedCount

/**
 *Created by Ravi on 2020-06-16.
 */
interface LastMileView {

    fun onSuccess(unassignedCount: UnassignedCount?)

    fun onTabCount()

    fun onFailure(error : Throwable?)

    fun setCancelView()

    fun onCancelSuccess()

}