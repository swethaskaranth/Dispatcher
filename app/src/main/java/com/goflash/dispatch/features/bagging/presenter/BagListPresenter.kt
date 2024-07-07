package com.goflash.dispatch.features.bagging.presenter

import android.content.Context
import com.goflash.dispatch.features.bagging.view.BagListView
import com.goflash.dispatch.features.bagging.view.BagRowView

interface BagListPresenter {

    fun onAttachView(context: Context, view : BagListView)

    fun onDetachView()

    fun getBagList()

    fun getCount() : Int

    fun OnBindBagRowView(position : Int, holder: BagRowView)

    fun onDestinationSelected(position : Int)

    fun clearFilter(dest : Boolean)

    fun getBagById(str : String)

    fun onBagItemClicked(position : Int)
}