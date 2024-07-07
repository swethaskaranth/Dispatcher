package com.goflash.dispatch.features.lastmile.tripCreation.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.goflash.dispatch.R
import com.goflash.dispatch.features.lastmile.tripCreation.listeners.OnSummaryListener
import com.goflash.dispatch.model.SummaryItem
import com.goflash.dispatch.type.ShipmentType
import com.goflash.dispatch.type.TaskStatus

class SummaryAdapter(val context: Context, val list : List<SummaryItem>, val onItemSelctedListener : OnSummaryListener) : androidx.recyclerview.widget.RecyclerView.Adapter<SummaryAdapter.SummaryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): SummaryViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.summary_item, parent, false)
        return SummaryViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: SummaryViewHolder, position: Int) {

        when (list[position].status) {
            TaskStatus.CREATED.name -> holder.taskStatus.text = context.getString(R.string.unattempted)
            TaskStatus.UNFINISHED.name -> holder.taskStatus.text = context.getString(R.string.customer_not_available)
            else -> holder.taskStatus.text =  list[position].status
        }
        holder.taskCount.text = list[position].count.toString()

        holder.itemView.setOnClickListener {
            if (list[position].status == ShipmentType.PICKUP.name || list[position].status == ShipmentType.DELIVERY.name)
                onItemSelctedListener.onItemSelected(position,true)
            else
                onItemSelctedListener.onItemSelected(position,false)
        }

    }


    class SummaryViewHolder(val view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {

        val taskStatus: TextView = view.findViewById(R.id.taskStatus)
        val taskCount: TextView = view.findViewById(R.id.taskCount)

    }
}