package com.goflash.dispatch.features.dispatch.view

interface DispatchBagView {

    fun setBagCount(count : String)

    fun onFailure(error : Throwable?)

    fun onSealRequiedFetched(sealRequired : Boolean)

}