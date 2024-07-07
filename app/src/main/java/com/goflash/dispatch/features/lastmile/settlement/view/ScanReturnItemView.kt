package com.goflash.dispatch.features.lastmile.settlement.view

import com.goflash.dispatch.data.Item

interface ScanReturnItemView {

    fun onFailure(error : Throwable?)

    fun setItemCount(scanned : Int, total : Int)

    fun onScannedItemsFetched(list : List<Item>)



}