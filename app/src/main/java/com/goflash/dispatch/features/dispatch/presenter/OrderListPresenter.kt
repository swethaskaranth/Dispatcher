package com.goflash.dispatch.features.dispatch.presenter

import android.content.Context
import com.goflash.dispatch.presenter.views.CancelledRowView
import com.goflash.dispatch.features.dispatch.view.OrderListView

interface OrderListPresenter{

    fun onAttachView(context: Context, view: OrderListView)

    fun onDetachView()

    fun getAllOrders()

    fun onBindCanceeldRowView(position : Int, cancelledRowView: CancelledRowView)

    fun getCount() : Int
}