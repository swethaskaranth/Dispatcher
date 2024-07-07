package com.goflash.dispatch.features.lastmile.tripCreation.listeners

interface FragmentListener {

    fun deleteOrUnblockShipment(position: Int)

    fun callDeliveryAgent(phoneNumber: String)

    fun commonListener()

}