package com.goflash.dispatch.features.lastmile.tripCreation.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.goflash.dispatch.R
import com.goflash.dispatch.data.ChildShipmentDTO

class MPSBoxAdapater(val context: Context, val list: List<ChildShipmentDTO>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        val HEADER = 0
        val ITEM = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == HEADER) {
            val view =
                LayoutInflater.from(context).inflate(R.layout.layout_mps_box_header, parent, false)
            MPSBoxHeader(view)
        } else {
            val view =
                LayoutInflater.from(context).inflate(R.layout.layout_mps_box_item, parent, false)
            MPSBoxHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {


        if (position > 0){
            val lot = list[position-1]
            (holder as MPSBoxHolder).refId.text = lot.referenceId
            holder.lbn.text = lot.lbn
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

    class MPSBoxHolder(val view: View) :
        RecyclerView.ViewHolder(view) {
        val refId: TextView = view.findViewById(R.id.tvRefId)
        val lbn: TextView = view.findViewById(R.id.tvLBN)
    }

    class MPSBoxHeader(val view: View) :
        RecyclerView.ViewHolder(view) {

    }


}