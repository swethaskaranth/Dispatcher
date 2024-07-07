package com.goflash.dispatch.features.lastmile.tripCreation.view

interface MergeTripRowView {

    fun setTripId(id: String)

    fun setSprinterName(name: String?)

    fun setCount(shipmentCount: Long)

    fun setRadioOnChangeListener(position: Int)

    fun setRadio(check: Boolean)

}