package com.goflash.dispatch.ui.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.goflash.dispatch.R
import com.goflash.dispatch.presenter.CancelledPresenter
import com.goflash.dispatch.presenter.views.CancelledRowView

class CancelledAdapter(private val context: Context,private val cancelledRowPresenter: CancelledPresenter) : androidx.recyclerview.widget.RecyclerView.Adapter<CancelledAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CancelledAdapter.ViewHolder {

        val view = LayoutInflater.from(context).inflate(R.layout.cancelled_list_item, parent, false)
        return CancelledAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: CancelledAdapter.ViewHolder, position: Int) {

        cancelledRowPresenter.onBindCanceeldRowView(position,holder)

       /* val lot = items[position]

        holder.order_id.text = "Order Id #${lot.referenceId}"
        holder.cancelled_item_layout.setBackgroundColor(Color.parseColor("#" + lot.colourCode))

        if (lot.isScanned)
            holder.checked.visibility = View.VISIBLE
        else
            holder.checked.visibility = View.GONE

        if (items.size == 1)
            holder.checked.visibility = View.VISIBLE*/


    }

    override fun getItemCount(): Int {
        return cancelledRowPresenter.getCount()
    }

    class ViewHolder(v: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(v), CancelledRowView {
        val view: View = v
        val order_id: TextView = view.findViewById(R.id.order_id)
        val cancelled_item_layout: RelativeLayout = view.findViewById(R.id.cancelled_item_layout)
        val checked: ImageView = view.findViewById(R.id.checked)
        val tvLbn : TextView = view.findViewById(R.id.lbn)

        override fun setOrderId(orderId: String) {
            order_id.text = orderId
        }

        override fun setBackgroundColor(color: String) {
            cancelled_item_layout.setBackgroundColor(Color.parseColor(color))

        }

        override fun checkOrUncheck(visible: Boolean) {
            if (visible)
                checked.visibility = View.VISIBLE
            else
                checked.visibility = View.GONE
        }

        override fun setLBN(lbn: String) {
            tvLbn.text = lbn
        }

    }
}
