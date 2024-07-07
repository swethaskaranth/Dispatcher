package com.goflash.dispatch.features.lastmile.tripCreation.view

import com.goflash.dispatch.data.UnassignedDTO
import com.goflash.dispatch.model.AddressDTO
import com.goflash.dispatch.model.ShipmentDTO

interface ScanToSearchView {

    fun onSuccess(list: List<UnassignedDTO>)

    fun onFailure(error : Throwable?)

    fun updateList(position: Int, detail: AddressDTO)

}