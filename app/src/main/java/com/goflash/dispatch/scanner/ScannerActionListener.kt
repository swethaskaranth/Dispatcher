package com.goflash.dispatch.scanner

interface ScannerActionListener {

    fun onConnected()

    fun onConnecting()

    fun onDisconnected()

    fun onData(barcode: String)
}