package com.goflash.dispatch.features.receiving.view

interface RemoveBagsView {

    fun setCount(count : Int)

    fun refreshList()

    fun showSnackBar(message : String)

    fun onFailure(error : Throwable?)

    fun finishActivity(vehicleId: String?, tripId: String?)

}