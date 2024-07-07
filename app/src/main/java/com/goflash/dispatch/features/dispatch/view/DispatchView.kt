package com.goflash.dispatch.features.dispatch.view

interface DispatchView{

    fun onSuccessOrderScan(isDispatchable : Boolean)

    fun takeToCancelledActivity(singleOrderScanned: Boolean)

    fun takeToDispatchBagActivity()

    fun onFailure(error: Throwable?)



}