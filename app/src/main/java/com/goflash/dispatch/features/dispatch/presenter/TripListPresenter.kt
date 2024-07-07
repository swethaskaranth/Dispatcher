package com.goflash.dispatch.features.dispatch.presenter

import android.content.Context
import com.goflash.dispatch.features.dispatch.view.TripListView
import com.goflash.dispatch.features.dispatch.view.TripRowView

interface TripListPresenter {

    fun onAttachView(context: Context, view: TripListView)

    fun onDetachView()

    fun getTripList()

    fun getCount() : Int

    fun onBindTripRowView(position : Int, holder : TripRowView)

}