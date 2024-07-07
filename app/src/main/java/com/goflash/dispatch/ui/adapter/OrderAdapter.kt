package com.goflash.dispatch.ui.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import com.goflash.dispatch.R
import com.goflash.dispatch.features.dispatch.presenter.OrderListPresenter
import com.goflash.dispatch.presenter.views.CancelledRowView

class OrderAdapter(private val context: Context, private val presenter : OrderListPresenter) : androidx.recyclerview.widget.RecyclerView.Adapter<OrderAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderAdapter.ViewHolder {

        val view = LayoutInflater.from(context).inflate(R.layout.cancelled_list_item, parent, false)
        return OrderAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderAdapter.ViewHolder, position: Int) {

        presenter.onBindCanceeldRowView(position,holder)
       /* val lot = items[position]

        holder.order_id.text = "Order Id #${lot.referenceId}"

        if(lot.colourCode != null && !lot.colourCode.isEmpty())
            holder.cancelled_item_layout.setBackgroundColor(Color.parseColor("#${lot.colourCode}"))*/

    }

    override fun getItemCount(): Int {
        return presenter.getCount()
    }

    class ViewHolder(v: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(v), CancelledRowView {
        val view: View = v
        val order_id : TextView = view.findViewById(R.id.order_id)
        val cancelled_item_layout : RelativeLayout = view.findViewById(R.id.cancelled_item_layout)
        val tvLbn : TextView = view.findViewById(R.id.lbn)


        override fun setOrderId(orderId: String) {
            order_id.text = orderId
        }

        override fun setLBN(lbn: String) {
            tvLbn.text = lbn
        }

        override fun setBackgroundColor(color: String) {
            cancelled_item_layout.setBackgroundColor(Color.parseColor(color))
        }

        override fun checkOrUncheck(visible: Boolean) {

        }

    }
}