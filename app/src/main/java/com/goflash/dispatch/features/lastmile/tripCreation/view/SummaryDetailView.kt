package com.goflash.dispatch.features.lastmile.tripCreation.view

import com.goflash.dispatch.data.TaskListDTO
import com.goflash.dispatch.model.AddressDTO

interface SummaryDetailView {

    fun onFailure(error: Throwable)

    fun updateList(position: Int, detail: AddressDTO)

}