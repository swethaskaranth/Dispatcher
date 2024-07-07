package com.goflash.dispatch.features.bagging.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.goflash.dispatch.R
import com.goflash.dispatch.features.bagging.presenter.ScannedShipmentsPresenter
import com.goflash.dispatch.presenter.views.CancelledRowView

class ScannedShipmentAdapter(private val context: Context, private val scannedShipmentsPresenter: ScannedShipmentsPresenter) : androidx.recyclerview.widget.RecyclerView.Adapter<ScannedShipmentAdapter.ScannedShipmentHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScannedShipmentHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.cancelled_list_item, parent, false)
        return ScannedShipmentHolder(view)
    }

    override fun getItemCount(): Int {
        return scannedShipmentsPresenter.getCount()
    }

    override fun onBindViewHolder(holder: ScannedShipmentHolder, position: Int) {
        scannedShipmentsPresenter.onBindCanceeldRowView(position,holder)
    }


    class ScannedShipmentHolder(v: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(v), CancelledRowView {

        val view: View = v
        val order_id: TextView = view.findViewById(R.id.order_id)
        val tvlbn : TextView = view.findViewById(R.id.lbn)

        override fun setOrderId(orderId: String) {
            order_id.text = orderId
        }

        override fun setBackgroundColor(color: String) {

        }

        override fun checkOrUncheck(visible: Boolean) {

        }

        override fun setLBN(lbn: String) {
            tvlbn.text = lbn
        }


    }
}