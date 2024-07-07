package com.goflash.dispatch.features.lastmile.tripCreation.view

import com.goflash.dispatch.data.UnassignedDTO
import com.goflash.dispatch.model.AddressDTO
import com.goflash.dispatch.model.ShipmentDTO

interface UnassignedView {

    fun displayProgress()

    fun onShipmentsFetched(list: List<UnassignedDTO>)

    fun onFailure(error : Throwable?)

    fun showIntransitCount(count: Int)

    fun updateList(position: Int, detail: AddressDTO)

}