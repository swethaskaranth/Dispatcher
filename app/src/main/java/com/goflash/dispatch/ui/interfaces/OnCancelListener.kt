package com.goflash.dispatch.ui.interfaces

interface OnCancelListener{

    fun onSuccess(reason : String){}

    fun onError(error: Throwable?){}

    fun onError(message: String){}
}