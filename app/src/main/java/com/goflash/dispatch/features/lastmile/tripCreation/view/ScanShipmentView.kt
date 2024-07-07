package com.goflash.dispatch.features.lastmile.tripCreation.view

import com.goflash.dispatch.data.TaskListDTO
import com.goflash.dispatch.model.AddressDTO

interface ScanShipmentView {

    fun onShipmentsFetched(list : List<TaskListDTO>)

    fun onFailure(error : Throwable?)

    fun onAddRemoveSuccess(add : Boolean)

    fun onTripDeleteSuccess()

    fun updateList(position: Int, detail: AddressDTO)

}