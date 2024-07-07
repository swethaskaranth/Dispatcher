package com.goflash.dispatch.features.lastmile.tripCreation.view

import com.goflash.dispatch.data.SprinterList

interface SelectSprinterView {

    fun onSprintersFetched(list : List<SprinterList>)

    fun onFailure(error : Throwable?)

    fun onCreateTripSuccess(tripId : Long,sprinterName : String)

    fun onAssignSuccess()
}