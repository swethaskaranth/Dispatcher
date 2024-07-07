package com.goflash.dispatch.features.receiving.view

/**
 *Created by Ravi on 2019-09-08.
 */
interface CompleteScanView {

    fun onFailure(error : Throwable?)

    fun onSuccess()

    fun onShowProgress()

    fun onHideProgress()

    fun disableScanner(tripId : String, sprinter : String, time : String)

    fun showErrorAndRedirect(message: String)

}