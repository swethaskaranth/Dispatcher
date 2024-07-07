package com.goflash.dispatch.features.lastmile.tripCreation.listeners

interface SelectedListener {

    fun onShipmentSelected(position : Int, view: Int)

    fun onCallDeliveryAgent(number: String)
}