package com.goflash.dispatch.features.lastmile.tripCreation.presenter

import android.content.Context
import com.goflash.dispatch.features.lastmile.tripCreation.view.LastMileView

interface LastMilePresenter{

    fun onAttachView(context: Context, view: LastMileView)

    fun onDetachView()

    fun getUnassignedCount()

    fun getTabCount()

    fun cancelSmartTrip()

}