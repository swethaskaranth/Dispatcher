package com.goflash.dispatch.features.lastmile.tripCreation.view

import com.goflash.dispatch.data.TripDTO

/**
 *Created by Ravi on 2020-06-16.
 */
interface CreatedView {

    fun onSuccess(tripList: List<TripDTO>)

    fun onFailure(error : Throwable?)

    fun onEditClick(data: TripDTO, screen: Int)

    fun onListClick(data: TripDTO)

    fun onMergeClick(data: Long)


}