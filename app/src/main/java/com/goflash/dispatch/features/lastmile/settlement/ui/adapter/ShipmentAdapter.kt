package com.goflash.dispatch.features.lastmile.settlement.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.goflash.dispatch.R

class ShipmentAdapter (private val context: Context, private val list : List<String>) : RecyclerView.Adapter<ShipmentAdapter.ShipmentHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShipmentHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_settlement_shipment_item,parent,false)
        return ShipmentHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ShipmentHolder, position: Int) {
        val refId = list[position]

        holder.refId.text = String.format(context.getString(R.string.reference_id),refId)
    }


    class ShipmentHolder(view : View) : RecyclerView.ViewHolder(view){

        val refId : TextView = view.findViewById(R.id.tvRefId)
    }


}