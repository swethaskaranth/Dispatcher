package com.goflash.dispatch.features.rtoReceiving.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.goflash.dispatch.R
import com.goflash.dispatch.data.InwardRunItem

class MpsRunitemAdapter(private val context: Context, private val list: List<InwardRunItem>) :
    RecyclerView.Adapter<MpsRunitemAdapter.MpsRunitemHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MpsRunitemHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_mps_run_item, parent, false)
        return MpsRunitemHolder(view)
    }

    override fun onBindViewHolder(holder: MpsRunitemHolder, position: Int) {
        val item = list[position]
        holder.tvRefId.text = item.wayBillNumber ?: item.referenceId ?: item.lbn
    }

    override fun getItemCount(): Int {
        return list.size
    }


    class MpsRunitemHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvRefId: TextView = view.findViewById(R.id.tvRefId)
    }
}