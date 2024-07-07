package com.goflash.dispatch.features.lastmile.tripCreation.presenter

import android.content.Context
import com.goflash.dispatch.features.lastmile.tripCreation.view.CreatedView
import com.goflash.dispatch.features.lastmile.tripCreation.view.StatusRowView

interface CreatedPresenter{

    fun onAttachView(context: Context, view: CreatedView)

    fun onDetachView()

    fun getCreatedTrips()

    fun getBagList()

    fun getCount() : Int

    fun onStatusRowView(position : Int, holder: StatusRowView)

    fun onClickListner(id: Int, screen: Int)

    fun onMergeClicked(position: Int)

}