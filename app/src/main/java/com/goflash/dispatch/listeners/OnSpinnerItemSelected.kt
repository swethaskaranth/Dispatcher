package com.goflash.dispatch.listeners

import com.goflash.dispatch.data.VehicleDetails

interface OnSpinnerItemSelected{

    fun onItemSelected(position : Int, spinnerPosition: Int, reason: MutableList<String>, lot: VehicleDetails)
}