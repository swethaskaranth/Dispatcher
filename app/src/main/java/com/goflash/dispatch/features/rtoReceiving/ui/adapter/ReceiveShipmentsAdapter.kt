package com.goflash.dispatch.features.rtoReceiving.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.goflash.dispatch.R
import com.goflash.dispatch.data.InwardRun
import com.goflash.dispatch.features.rtoReceiving.listeners.RunitemSelectionListener
import com.goflash.dispatch.util.getDateTime

class ReceiveShipmentsAdapter(
    private val context: Context,
    private val list: List<InwardRun>,
    private val listener: RunitemSelectionListener
) : RecyclerView.Adapter<ReceiveShipmentsAdapter.ReceiveShipmentHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReceiveShipmentHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.receive_shipment_item,parent,false)
        return ReceiveShipmentHolder(view)
    }

    override fun onBindViewHolder(holder: ReceiveShipmentHolder, position: Int) {
        val item = list[position]
        holder.tvId.text = "${item.id} | ${item.partnerName}"
        holder.tvShipmentCount.text = "${item.scannedItemCount}"
        holder.tvUsername.text = "${item.createdByName}"
        holder.tvReceivingTime.text = item.createdOn?.let { getDateTime(it) }

        holder.imageView.setOnClickListener { listener.onRunItemSelcted(item.id) }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ReceiveShipmentHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvId: TextView = view.findViewById(R.id.tvId)
        val tvShipmentCount: TextView = view.findViewById(R.id.tvShipmentCount)
        val tvUsername: TextView = view.findViewById(R.id.tvUsername)
        val tvReceivingTime : TextView = view.findViewById(R.id.tvReceivingTime)
        val imageView: ImageView = view.findViewById(R.id.imageView)

    }
}