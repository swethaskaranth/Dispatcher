package com.goflash.dispatch.features.lastmile.settlement.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.goflash.dispatch.R
import com.goflash.dispatch.data.PoaResponseForRecon

class AckDeliverySlipAdapter(
    private val context: Context,
    private val list: List<PoaResponseForRecon>
) : RecyclerView.Adapter<AckDeliverySlipAdapter.AckDeliverySlipViewHolder>() {

    companion object {
        const val TYPE_HEADER = 0
        const val TYPE_DETAIL = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AckDeliverySlipViewHolder {
        return if (viewType == TYPE_HEADER) {
            val view = LayoutInflater.from(context)
                .inflate(R.layout.ack_delivery_slip_header, parent, false)
            AckDeliverySlipViewHolder.HeadersHolder(view)
        } else {
            val view =
                LayoutInflater.from(context).inflate(R.layout.ack_delivery_slip_item, parent, false)
            AckDeliverySlipViewHolder.DetailsHolder(view)
        }
    }

    override fun onBindViewHolder(holder: AckDeliverySlipViewHolder, position: Int) {
        if(holder is AckDeliverySlipViewHolder.DetailsHolder){
            val item = list[position-1]
            holder.referenceId.text = item.referenceId
            holder.lbn.text = item.lbn
            holder.scanned.visibility = if (item.isScanned) View.VISIBLE else View.GONE
        }
    }

    override fun getItemCount(): Int = list.size.plus(1)

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) TYPE_HEADER else TYPE_DETAIL
    }

    sealed class AckDeliverySlipViewHolder(view: View) : ViewHolder(view) {
        class HeadersHolder(view: View) : AckDeliverySlipViewHolder(view)

        class DetailsHolder(view: View) : AckDeliverySlipViewHolder(view) {
            val referenceId: TextView = view.findViewById(R.id.tvRefId)
            val lbn: TextView = view.findViewById(R.id.tvLbn)
            val scanned: ImageView = view.findViewById(R.id.ivScanned)
        }
    }

}