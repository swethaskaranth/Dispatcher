package com.goflash.dispatch.features.rtoReceiving.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.goflash.dispatch.R
import com.goflash.dispatch.data.InwardRunItem
import com.goflash.dispatch.listeners.OnItemSelected

class RunsheetAdapter(private val context: Context, private val list: List<InwardRunItem>, private val listener: OnItemSelected) :
    RecyclerView.Adapter<RunsheetAdapter.RunsheetItemHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RunsheetItemHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.layout_runsheet_item, parent, false)
        return RunsheetItemHolder(view)
    }

    override fun onBindViewHolder(holder: RunsheetItemHolder, position: Int) {
        val item = list[position]

        holder.tvReferenceId.text = item.wayBillNumber?: item.referenceId
        val shipmentStatus = item.shipmentStatus?.replace("_"," ")
        holder.status.text =
            if (item.returnType?.toLowerCase() != "forward") "${item.returnType}/${shipmentStatus}" else shipmentStatus
        if (item.multipartShipment) {
            holder.tvReferenceId.text = "${item.mpsParentLbn}"
            holder.mpsCount.visibility = View.VISIBLE
            holder.mpsCount.text =
                String.format(context.getString(R.string.mps_count), item.mpsScannedCount)
        } else
            holder.mpsCount.visibility = View.GONE

        holder.mpsCount.setOnClickListener { listener.onItemSelected(position) }

    }


    override fun getItemCount(): Int {
        return list.size
    }


    class RunsheetItemHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvReferenceId: TextView = view.findViewById(R.id.tvReferenceId)
        val mpsCount: TextView = view.findViewById(R.id.tvShipmentCount)
        val status: TextView = view.findViewById(R.id.tvStatus)
    }


}