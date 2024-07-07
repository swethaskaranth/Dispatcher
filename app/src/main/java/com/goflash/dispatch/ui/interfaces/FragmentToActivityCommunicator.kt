package com.goflash.dispatch.ui.interfaces

interface FragmentToActivityCommunicator {

    fun onSuccess(){}

    fun onError(error: Throwable?){}

    fun onError(message: String){}

}