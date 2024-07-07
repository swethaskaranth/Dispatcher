package com.goflash.dispatch.features.dispatch.presenter.impl

import android.content.Context
import co.uk.rushorm.core.RushSearch
import com.goflash.dispatch.R
import com.goflash.dispatch.data.ScannedOrder
import com.goflash.dispatch.features.dispatch.presenter.OrderListPresenter
import com.goflash.dispatch.presenter.views.CancelledRowView
import com.goflash.dispatch.features.dispatch.view.OrderListView
import rx.subscriptions.CompositeSubscription

class OrderListPresenterImpl : OrderListPresenter {

    private val TAG = OrderListPresenterImpl::class.java.simpleName

    private var orderListView: OrderListView? = null

    private var compositeSubscription: CompositeSubscription? = null

    private var orders: MutableList<ScannedOrder> = mutableListOf()

    private var context: Context? = null

    override fun onAttachView(context: Context, view: OrderListView) {
        this.orderListView = view
        this.context = context
        compositeSubscription = CompositeSubscription()

        getAllOrders()
    }

    override fun onDetachView() {
        if (orderListView == null)
            return
        orderListView = null
        compositeSubscription?.unsubscribe()
        compositeSubscription = null
    }

    override fun getAllOrders() {
        orders = RushSearch().find(ScannedOrder::class.java)

        orderListView?.showOrderCount(orders.get(0).binNumber)
        /*if(orders != null && orders.size > 0)
            orderListView?.onOrdersFetched(orders)
        else
            orderListView?.onFailure("No Orders Found")*/

    }

    override fun onBindCanceeldRowView(position: Int, cancelledRowView: CancelledRowView) {
        val order = orders[position]

        if (order.packageId != null && order.packageId.isNotEmpty())
        cancelledRowView.setOrderId(
            String.format(
                context!!.getString(R.string.package_id_label),
                order.packageId
            )
        )
        else
        cancelledRowView.setOrderId(
            String.format(
                context!!.getString(R.string.reference_id_label),
                order.referenceId
            )
        )
        cancelledRowView.setLBN(String.format(context!!.getString(R.string.lbn_label), order.lbn))

        if (order.colourCode != null && !order.colourCode.isEmpty())
            cancelledRowView.setBackgroundColor("#${order.colourCode}")
    }

    override fun getCount(): Int {
        return orders.size
    }

}