package com.goflash.dispatch.features.lastmile.tripCreation.view

import com.goflash.dispatch.data.TripDTO

/**
 *Created by Ravi on 2020-06-16.
 */
interface OfdView {

    fun onSuccess(tripList: List<TripDTO>)

    fun onFailure(error : Throwable?)
}