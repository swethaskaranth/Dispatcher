package com.goflash.dispatch.features.rtoReceiving.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.goflash.dispatch.R
import com.goflash.dispatch.data.PartnerNameDTO
import com.goflash.dispatch.listeners.OnItemSelected

class PartnerNameAdapter(
    private val context: Context,
    private val list: List<PartnerNameDTO>,
    private val listener: OnItemSelected
) : RecyclerView.Adapter<PartnerNameAdapter.PartnerNameHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PartnerNameHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_partner_name, parent, false)
        return PartnerNameHolder(view)
    }

    override fun onBindViewHolder(holder: PartnerNameHolder, position: Int) {
        val item = list[position]

        holder.tvPartnerName.text = item.name
        holder.tvShipmentCount.text = "${item.count}"

        if(item.selected){
            holder.tvPartnerName.setTextColor(ContextCompat.getColor(context, R.color.white))
            holder.itemView.setBackgroundResource(R.drawable.partner_name_background_selected)
        }else{
            holder.tvPartnerName.setTextColor(ContextCompat.getColor(context, R.color.text_color_blue))
            holder.itemView.setBackgroundResource(R.drawable.partner_name_background)
        }

        holder.itemView.setOnClickListener { listener.onItemSelected(position) }

    }

    override fun getItemCount(): Int {
        return list.size
    }


    class PartnerNameHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvPartnerName: TextView = view.findViewById(R.id.tvPartnerName)
        val tvShipmentCount: TextView = view.findViewById(R.id.tvShipmentCount)
    }
}