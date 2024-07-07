package com.goflash.dispatch.features.lastmile.settlement.view

import com.goflash.dispatch.data.Item

interface ReviewItemView {

    fun setItemDetails(name : String?,batch : String?, reason : String?)

    fun onItemsFetched(items : List<Item>)

    fun showError(message: String)

}