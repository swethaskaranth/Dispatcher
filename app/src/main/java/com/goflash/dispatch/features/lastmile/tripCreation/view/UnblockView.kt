package com.goflash.dispatch.features.lastmile.tripCreation.view

/**
 *Created by Ravi on 2020-06-16.
 */
interface UnblockView {

    fun onSuccess()

    fun onFailure(error : Throwable?)
}