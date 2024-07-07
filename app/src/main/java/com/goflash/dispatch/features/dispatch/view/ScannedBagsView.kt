package com.goflash.dispatch.features.dispatch.view

interface ScannedBagsView {

    fun setCount(count : Int)

    fun refreshList()

    fun showSnackBar(message : String)

    fun onFailure(error : Throwable?)

    fun finishActivity()

}