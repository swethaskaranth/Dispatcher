package com.goflash.dispatch.features.lastmile.settlement.view

interface Step3View {

    fun onSuccess(message: String)

    fun onFailure(error : Throwable?)

}