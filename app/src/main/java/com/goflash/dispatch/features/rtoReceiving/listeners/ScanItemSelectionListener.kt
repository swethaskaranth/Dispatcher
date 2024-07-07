package com.goflash.dispatch.features.rtoReceiving.listeners

interface ScanItemSelectionListener {

    fun onActionButtonClicked(position: Int)

    fun onShipmentCountClicked(position: Int) { }
}