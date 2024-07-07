package com.goflash.dispatch.features.rtoReceiving.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.goflash.dispatch.R
import com.goflash.dispatch.data.InwardRunItem
import com.goflash.dispatch.features.rtoReceiving.listeners.ScanItemSelectionListener

class ReceiveMpsShipmentAdapter(
    private val context: Context,
    private val list: List<InwardRunItem>,
    private val listener: ScanItemSelectionListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        val HEADER = 0
        val ITEM = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == HEADER) {
            val view =
                LayoutInflater.from(context)
                    .inflate(R.layout.layout_mps_receive_box_header, parent, false)
            MpsShipmentHeader(view)
        } else {
            val view =
                LayoutInflater.from(context)
                    .inflate(R.layout.layout_mps_receiving_item, parent, false)
            MpsShipmentHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (position > 0) {
            val item = list[position - 1]
            (holder as MpsShipmentHolder).tvReferenceId.text =
                item.wayBillNumber ?: item.referenceId ?: item.lbn
            if (item.id != -1) {
                holder.tvStatus.visibility = View.VISIBLE
                holder.ivScanned.visibility = View.VISIBLE
                if (item.status == "REJECT") {
                    holder.tvStatus.text = context.getString(R.string.rejected)
                    holder.tvStatus.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.reject_background
                        )
                    )
                    holder.ivAction.setImageDrawable(
                        ContextCompat.getDrawable(
                            context,
                            R.drawable.ic_rejected
                        )
                    )
                } else {
                    holder.ivAction.setImageDrawable(
                        ContextCompat.getDrawable(
                            context,
                            R.drawable.ic_warning
                        )
                    )
                    holder.tvStatus.text = context.getString(R.string.accepted)
                    holder.tvStatus.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.green_border_color
                        )
                    )
                }
                holder.ivAction.setOnClickListener {
                    if (item.id != -1)
                        listener.onActionButtonClicked(position - 1)
                }
            } else {
                holder.ivScanned.visibility = View.INVISIBLE
               when(item.status){
                    "ACCEPT" -> {
                        holder.tvStatus.text = "Accepted"
                        holder.tvStatus.setTextColor(
                            ContextCompat.getColor(
                                context,
                                R.color.green_border_color
                            )
                        )
                    }
                    "REJECT" -> {
                        holder.tvStatus.text = "Rejected"
                        holder.tvStatus.setTextColor(
                            ContextCompat.getColor(
                                context,
                                R.color.reject_background
                            )
                        )
                    }
                   else -> holder.tvStatus.text = ""
                }
                holder.ivAction.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.ic_disabled
                    )
                )
            }

        }
    }

    override fun getItemCount(): Int {
        return list.size.plus(1)
    }


    override fun getItemViewType(position: Int): Int {
        return if (position == 0)
            HEADER
        else
            ITEM
    }

    fun updateRunItem(item: InwardRunItem) {
        val runItem = list.find { it.id == item.id }
        runItem?.status = item.status
        notifyDataSetChanged()
    }

    class MpsShipmentHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivAction: ImageView = view.findViewById(R.id.ivAction)
        val tvReferenceId: TextView = view.findViewById(R.id.tvReferenceId)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val ivScanned: ImageView = view.findViewById(R.id.ivScanned)
    }

    class MpsShipmentHeader(view: View) : RecyclerView.ViewHolder(view) {

    }


}