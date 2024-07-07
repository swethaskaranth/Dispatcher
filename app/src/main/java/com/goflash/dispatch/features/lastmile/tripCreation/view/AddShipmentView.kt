package com.goflash.dispatch.features.lastmile.tripCreation.view

import com.goflash.dispatch.data.UnassignedDTO
import com.goflash.dispatch.model.AddressDTO

interface AddShipmentView {

    fun displayProgress()

    fun onFailure(error : Throwable?)

    fun onShipmentsFetched(shipments : Map<String,List<UnassignedDTO>>)

    fun onAddSuccess()

    fun showIntransitCount(count: Int)

    fun updateList(position: Int, detail: AddressDTO)

}