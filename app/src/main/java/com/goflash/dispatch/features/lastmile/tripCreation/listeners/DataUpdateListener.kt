package com.goflash.dispatch.features.lastmile.tripCreation.listeners

import com.goflash.dispatch.data.TaskListDTO

interface DataUpdateListener {

    fun onViewDetails(position : Int, data: TaskListDTO)

    fun onItemSelected(position: Int){}

}