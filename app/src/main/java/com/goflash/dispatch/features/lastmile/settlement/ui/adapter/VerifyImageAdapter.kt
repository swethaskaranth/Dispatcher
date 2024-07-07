package com.goflash.dispatch.features.lastmile.settlement.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.goflash.dispatch.R
import com.goflash.dispatch.data.AckForRecon
import com.goflash.dispatch.listeners.VerifyImageListener
import com.goflash.dispatch.type.AckStatus

class VerifyImageAdapter(
    private val context: Context,
    private val ackListMap: MutableList<AckForRecon>,
    private val listener: VerifyImageListener,
) : RecyclerView.Adapter<VerifyImageAdapter.VerifyImageHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerifyImageHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.layout_review_order_item, parent, false)
        return VerifyImageHolder(view)
    }

    override fun onBindViewHolder(holder: VerifyImageHolder, position: Int) {
        val item = ackListMap[position]
        holder.tvOrderId.text = item.displayId

        val list = item.ackList
        list?.let { ackList ->
            val approvedCount = ackList.filter { it.status == AckStatus.ACCEPTED.name }.size
            if(approvedCount > 0) {
                holder.txtApprovedCount.text = context.getString(R.string.approved_count_text, approvedCount, ackList.size)
                holder.txtApprovedCount.visibility = View.VISIBLE
                holder.ivChecked.visibility = View.VISIBLE
            }else{
                holder.txtApprovedCount.visibility = View.GONE
                holder.ivChecked.visibility = View.GONE
            }

        }

        holder.itemView.setOnClickListener {
            listener.onOrderSelected(item.lbn)
        }
    }

    override fun getItemCount(): Int {
        return ackListMap.size
    }

    class VerifyImageHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvOrderId: TextView = view.findViewById(R.id.tvOrderId)
        val ivChecked: ImageView = view.findViewById(R.id.ivChecked)
        val txtApprovedCount: TextView = view.findViewById(R.id.txtApprovedCount)
    }
}