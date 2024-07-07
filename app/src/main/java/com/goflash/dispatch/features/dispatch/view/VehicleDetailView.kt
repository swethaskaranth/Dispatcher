package com.goflash.dispatch.features.dispatch.view

import com.goflash.dispatch.data.Sprinter

interface VehicleDetailView {

    fun onSprinterListFetched(sprinters : MutableList<String>, btnText : String)

    fun onFailure(error : Throwable?)

    fun startVehcileSealScanActivity(selectedSprinter: Sprinter)

    fun enableProceedBtn()

    fun onSuccess(message : String, tripId : String,sprinter : String, invoiceRequired : Boolean)

    fun showInvalidVehicleNumber()

    fun setVehicleNumber(vehicleNumber: String?, showSuccess: Boolean, showNotMapped: Boolean = true)

    fun enableOrDisableVehicleNumber(modeChanged: Boolean = false)

    fun disableVehicleNumber()

}