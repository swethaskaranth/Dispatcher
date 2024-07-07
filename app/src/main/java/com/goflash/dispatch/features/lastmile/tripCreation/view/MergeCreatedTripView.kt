package com.goflash.dispatch.features.lastmile.tripCreation.view

import com.goflash.dispatch.data.TripDTO

interface MergeCreatedTripView {

    fun onFailure(error : Throwable?)

    fun refreshList()

    fun enableMerge(enable: Boolean)

    fun onSuccess()

}