package com.goflash.dispatch.features.bagging.view

interface BagListView {

    fun onFailure(error : Throwable?)

    fun refreshList()

    fun setCount(count : Int)

    fun setSpinner(list : MutableList<String>)

    fun startDiscardBagActivity(bagId : String)
}