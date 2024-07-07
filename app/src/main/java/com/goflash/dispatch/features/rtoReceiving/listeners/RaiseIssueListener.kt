package com.goflash.dispatch.features.rtoReceiving.listeners

interface RaiseIssueListener {

    fun onStatusSelected(status: String, wayBillNumber: String, exceptions: List<String> = mutableListOf())

}