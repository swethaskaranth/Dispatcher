package com.goflash.dispatch.features.lastmile.tripCreation.listeners

interface OnItemSelctedListener {

    fun onItemSelected(position : Int){}

    fun onShipmentSelected(originName: String){}

    fun onItemSelected(url: String, name: String){}
}