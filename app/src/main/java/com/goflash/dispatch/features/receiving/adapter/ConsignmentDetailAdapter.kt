package com.goflash.dispatch.features.receiving.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.goflash.dispatch.R
import com.goflash.dispatch.data.ShipmentCount

class ConsignmentDetailAdapter (private val context: Context,private val list : ArrayList<ShipmentCount>) : androidx.recyclerview.widget.RecyclerView.Adapter<ConsignmentDetailAdapter.ViewHolder>(){


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_bag_count,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val lot = list[position]

        holder.bagId.text = lot.bagId
        holder.shipmentCount.text = "${lot.shipmentCount}"
    }


    class ViewHolder(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view){

        val bagId = view.findViewById<TextView>(R.id.bag_id)
        val shipmentCount = view.findViewById<TextView>(R.id.shipment_count)


    }
}