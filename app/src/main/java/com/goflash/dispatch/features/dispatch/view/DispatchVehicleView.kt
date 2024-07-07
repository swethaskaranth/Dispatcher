package com.goflash.dispatch.features.dispatch.view

import com.goflash.dispatch.data.Invoice
import com.goflash.dispatch.data.MidMileDispatchedRunsheet
import java.util.ArrayList

interface DispatchVehicleView {

    fun onSuccess(message : String, invoiceRequired : Boolean)

    fun setBarcodeScanned(isbarcodeScanned : Boolean)

    fun printFromUrl(name: String, url:String)

    fun onFailure(error : Throwable?)

    fun onPrintSuccess()

    fun enableHomeButton()

    fun setupProceedButton(invoiceRequired: Boolean, enable : Boolean)

    fun enableordisableScanner(enable : Boolean,tripId : String, sprinter : String, time : String)

    fun onListFetched(list: ArrayList<Invoice>)

}