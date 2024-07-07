package com.goflash.dispatch.features.lastmile.tripCreation.view

import com.goflash.dispatch.data.TripDTO

/**
 *Created by Ravi on 2020-06-16.
 */
interface StatusRowView {

    fun bagId(id: Long)

    fun onListClick(position: Int)

    fun setCount(id: String)

    fun setName(name: String? = null, routeId: String?)

    fun setBin(id: String? = null)

    fun onClickListner(position: Int, data: TripDTO)

    fun enableOrDisableClicks(enable: Boolean)

    fun hideMerge()

}