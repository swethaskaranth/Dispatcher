package com.goflash.dispatch.features.lastmile.tripCreation.view

import com.goflash.dispatch.data.UnassignedDTO

interface FilterView {

    fun onSuccess(list: List<UnassignedDTO>)

    fun onFailure(error : Throwable?)

}