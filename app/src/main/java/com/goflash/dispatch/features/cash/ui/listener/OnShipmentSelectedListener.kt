package com.goflash.dispatch.features.cash.ui.listener

import com.goflash.dispatch.data.CashDetails

interface OnShipmentSelectedListener {

    fun onShipmentSelected(shipments : List<CashDetails>)

    fun onShipmentUnselected(shipments : List<CashDetails>)

    fun showCashBreakup(cashClosingId: String, totalCash: String)
}