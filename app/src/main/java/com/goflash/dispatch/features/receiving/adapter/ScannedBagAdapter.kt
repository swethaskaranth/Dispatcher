package com.goflash.dispatch.features.receiving.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.goflash.dispatch.R
import com.goflash.dispatch.data.VehicleDetails

class ScannedBagAdapter(private val context : Context, val items: MutableList<VehicleDetails>) : androidx.recyclerview.widget.RecyclerView.Adapter<ScannedBagAdapter.ReceivingHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReceivingHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_scanned,parent,false)
        return ReceivingHolder(view)
    }

    override fun onBindViewHolder(holder: ReceivingHolder, position: Int) {
        val lot = items[position]

        holder.txtBagId.text = lot.bagId

    }

    override fun getItemCount(): Int {
        return items.size
    }

    class ReceivingHolder(v : View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(v){
        val view: View = v
        val txtBagId = view.findViewById<TextView>(R.id.txt_bagId)
    }
}