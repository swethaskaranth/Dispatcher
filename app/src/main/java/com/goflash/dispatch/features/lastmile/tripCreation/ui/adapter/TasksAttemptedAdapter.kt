package com.goflash.dispatch.features.lastmile.tripCreation.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.goflash.dispatch.R
import com.goflash.dispatch.data.TaskListDTO
import com.goflash.dispatch.features.lastmile.tripCreation.listeners.DataUpdateListener
import com.goflash.dispatch.type.ShipmentType
import com.goflash.dispatch.type.TaskStatus

/**
 * Created by Ravi on 24/01/19.
 */
class TasksAttemptedAdapter(private val context: Context, private val items: List<TaskListDTO>, private val listener : DataUpdateListener) : androidx.recyclerview.widget.RecyclerView.Adapter<TasksAttemptedAdapter.ViewHolder>() {

    private val green by lazy {
        ContextCompat.getColor(context, R.color.button_green)
    }

    private val orange by lazy {
        ContextCompat.getColor(context, R.color.orange_light)
    }

    private val red by lazy {
        ContextCompat.getColor(context, R.color.red)
    }

    private val light_blue by lazy {
        ContextCompat.getColor(context, R.color.color_c7)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(context).inflate(R.layout.list_address, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val lot = items[position]
        holder.view.tag = lot

        val orderId = if (lot.packageId != null && lot.packageId.isNotEmpty()) lot.packageId else lot.referenceId

        holder.orderId.text = "Order id #${orderId}"

        if(lot.address1 != null) {
            holder.tvLabelViewDetails.visibility = View.GONE
            holder.address.visibility = View.VISIBLE
            holder.address.text =
                (lot.name + ", " + lot.address1 + ", " + lot.address2 + ", " + lot.address3 + ", " + lot.city
                        + " " + lot.state + " " + lot.pincode)
        }else{
            holder.tvLabelViewDetails.visibility = View.VISIBLE
            holder.address.visibility = View.GONE
        }

        with(holder.taskType) {
            when (lot.status) {
                TaskStatus.POSTPONED.name -> setTextColor(orange)
                TaskStatus.CANCELLED.name -> setTextColor(red)
                TaskStatus.COMPLETED.name -> setTextColor(green)
                TaskStatus.UNFINISHED.name -> setTextColor(orange)
                TaskStatus.CANCELLED_BY_CUSTOMER.name -> setTextColor(red)
                TaskStatus.BLOCKED.name -> setTextColor(red)
                else -> visibility = View.GONE
            }
        }

        when (lot.status) {
            TaskStatus.POSTPONED.name -> holder.taskType.text = "${lot.type} - ${lot.status}"
            TaskStatus.UNFINISHED.name -> {
                holder.taskType.text = "${lot.type} - ${"CUSTOMER NOT AVAILABLE"}"
            }
            TaskStatus.CANCELLED.name -> {
                holder.taskType.text = "CANCELLED AT ${lot.type}"
            }
            TaskStatus.CANCELLED_BY_CUSTOMER.name -> {
                holder.taskType.text = context.getString(R.string.cancelled_by_customer)
                holder.address.setTextColor(light_blue)
            }
            TaskStatus.COMPLETED.name -> {
                if (lot.type == ShipmentType.PICKUP.name)
                    holder.taskType.text = context.getString(R.string.picked_up)
                else
                    holder.taskType.text = context.getString(R.string.delivered)
            }
            TaskStatus.BLOCKED.name -> {
                holder.taskType.text = context.getString(R.string.order_blocked)
                holder.address.setTextColor(light_blue)
            }
        }

        holder.tvLabelViewDetails.setOnClickListener {
            listener.onViewDetails(position, lot)
        }

    }

    override fun getItemCount(): Int {
        return items.size
    }

    class ViewHolder(v: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(v) {
        val view: View = v
        val taskType: TextView = v.findViewById(R.id.tvTaskType)
        val orderId: TextView = v.findViewById(R.id.tvTaskOrderNo)
        val address: TextView = v.findViewById(R.id.tvTaskAddress)
        val tvLabelViewDetails: TextView = view.findViewById(R.id.labelViewDetails)
    }
}