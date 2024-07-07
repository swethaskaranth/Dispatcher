package com.goflash.dispatch.features.rtoReceiving.listeners

interface ReasonSelectionListener {

    fun onReasonSelected(position: Int)

    fun onReasonUnselected(position: Int)
}