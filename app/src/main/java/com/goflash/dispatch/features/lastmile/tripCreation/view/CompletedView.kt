package com.goflash.dispatch.features.lastmile.tripCreation.view

import com.goflash.dispatch.data.TripDTO
import com.goflash.dispatch.data.TripSettlementDTO

/**
 *Created by Ravi on 2020-06-16.
 */
interface CompletedView {

    fun onSuccess(tripList: List<TripDTO>)

    fun onSuccessSettlement(tripSettlementDTO: TripSettlementDTO, tripId: Long)

    fun onFailure(error : Throwable?)

}