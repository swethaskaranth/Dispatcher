package com.goflash.dispatch.features.lastmile.settlement.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.goflash.dispatch.R
import com.goflash.dispatch.data.FmPickedShipment

class FmPickupAdapter(private val context: Context, private val list: List<FmPickedShipment>): RecyclerView.Adapter<FmPickupAdapter.FmPickupHolder>()  {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FmPickupHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.lauout_fm_pickup_scanned_item,parent,false)
        return FmPickupHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: FmPickupHolder, position: Int) {
        val shipment = list[position]
        holder.tvRefId.text = shipment.referenceId
        holder.tvLbn.text = shipment.lbn
        holder.tag.text = shipment.tag
        when(shipment.tag){
            "MID_MILE" -> holder.tag.setBackgroundResource(R.drawable.fm_tag_mid_mile)
            "LAST_MILE" -> holder.tag.setBackgroundResource(R.drawable.fm_tag_last_mile)
        }

    }

    class FmPickupHolder(view: View): RecyclerView.ViewHolder(view){
        val tvRefId : TextView = view.findViewById(R.id.tvRefId)
        val tvLbn : TextView = view.findViewById(R.id.tvLbn)
        val tag : TextView = view.findViewById(R.id.tvTag)
    }
}