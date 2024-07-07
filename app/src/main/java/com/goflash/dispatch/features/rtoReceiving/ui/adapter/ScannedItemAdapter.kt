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

class ScannedItemAdapter(
    private val context: Context,
    private val listener: ScanItemSelectionListener,
    private val list: MutableList<InwardRunItem> = mutableListOf()
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        val PACKAGE = 0
        val MPS = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == PACKAGE) {
            val view = LayoutInflater.from(context)
                .inflate(R.layout.layout_scanned_receive_item, parent, false)
            ScannedItemHolder(view)
        } else {
            val view = LayoutInflater.from(context)
                .inflate(R.layout.layout_scanned_mps_shipment, parent, false)
            ScannedMpsItemHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val item = list[position]
        if (item.multipartShipment) {
            (holder as ScannedMpsItemHolder).tvReferenceId.text = String.format(
                context.getString(R.string.mps_item_ref_id),
                item.scannedBarcode ?: item.mpsParentLbn,
                item.mpsScannedCount,
                item.mpsCount
            )
            // holder.shipmentCOunt.visibility = if (item.mpsCount > 0) View.VISIBLE else View.GONE
            holder.shipmentCOunt.text = String.format(
                context.getString(R.string.mps_count),
                item.mpsCount
            )

            holder.shipmentCOunt.setOnClickListener {
                listener.onShipmentCountClicked(position)
            }
            if (item.mpsCount == item.mpsScannedCount)
                holder.icon.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.ic_checked_ico
                    )
                )
        } else {
            (holder as ScannedItemHolder).tvReferenceId.text =
                item.scannedBarcode ?: item.wayBillNumber ?: item.referenceId
            val shipmentStatus = item.shipmentStatus?.replace("_", " ")
            holder.tvShipmentStatus.text =
                if (item.returnType?.toLowerCase() != "forward") "${item.returnType}/${shipmentStatus}" else shipmentStatus
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
                if (item.rejectFlowRequired)
                    listener.onActionButtonClicked(position)
            }
        }

    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getItemViewType(position: Int): Int {
        val item = list[position]
        return if (item.multipartShipment)
            MPS
        else PACKAGE
    }

    fun setItemsList(items: List<InwardRunItem>) {
        list.clear()
        list.addAll(items)
        notifyDataSetChanged()
    }

    fun addItem(item: InwardRunItem) {
        list.add(item)
        notifyItemInserted(list.size - 1)
    }

    fun setItem(item: InwardRunItem) {
        val runItem = list.find { it.id == item.id }
        runItem?.mpsScannedCount = item.mpsScannedCount
        notifyItemChanged(list.indexOf(runItem))
    }

    fun updateRunItem(item: InwardRunItem) {
        val runItem = list.find { it.id == item.id }
        runItem?.status = item.status
        notifyItemChanged(list.indexOf(runItem))
    }

    class ScannedItemHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivAction: ImageView = view.findViewById(R.id.ivAction)
        val tvReferenceId: TextView = view.findViewById(R.id.tvReferenceId)
        val tvShipmentStatus: TextView = view.findViewById(R.id.tvShipmentStatus)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val ivScanned: ImageView = view.findViewById(R.id.ivScanned)

    }

    class ScannedMpsItemHolder(view: View) : RecyclerView.ViewHolder(view) {

        val tvReferenceId: TextView = view.findViewById(R.id.tvReferenceId)
        val shipmentCOunt: TextView = view.findViewById(R.id.tvShipmentCount)
        val icon: ImageView = view.findViewById(R.id.ivIcon)

    }
}