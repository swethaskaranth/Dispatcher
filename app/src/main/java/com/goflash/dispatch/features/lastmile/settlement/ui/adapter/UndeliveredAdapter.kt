package com.goflash.dispatch.features.lastmile.settlement.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.goflash.dispatch.R
import com.goflash.dispatch.data.UndeliveredShipmentDTO
import com.goflash.dispatch.type.TaskStatus

class UndeliveredAdapter (private val context: Context, private val list : List<UndeliveredShipmentDTO>) : RecyclerView.Adapter<UndeliveredAdapter.ShipmentHolder>(){

    private val yellow by lazy {
        ContextCompat.getColor(context, R.color.high_priority_text_color)
    }

    private val red by lazy {
        ContextCompat.getColor(context, R.color.canc_border)
    }

    private val grey by lazy {
        ContextCompat.getColor(context, R.color.color_c7)
    }

    private val green by lazy {
        ContextCompat.getColor(context, R.color.green)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShipmentHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_undelivered,parent,false)
        return ShipmentHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ShipmentHolder, position: Int) {
        val data = list[position]

        holder.refId.text = String.format(context.getString(R.string.reference_id), data.referenceId)

        with(holder.view){
            when(data.shipmentStatus){
                TaskStatus.CANCELLED_BY_CUSTOMER.name -> setBackgroundColor(red)
                TaskStatus.CANCELLED.name -> setBackgroundColor(red)
                TaskStatus.BLOCKED.name -> setBackgroundColor(red)
                TaskStatus.UNFINISHED.name -> setBackgroundColor(grey)
                TaskStatus.POSTPONED.name -> setBackgroundColor(yellow)
                TaskStatus.CREATED.name -> setBackgroundColor(green)
            }
        }

        with(holder.reason){
            when(data.shipmentStatus){
                TaskStatus.CANCELLED_BY_CUSTOMER.name -> text = context.getString(R.string.reason_cancelled_by_customer)
                TaskStatus.UNFINISHED.name -> text = context.getString(R.string.reason_customer_not_available)
                TaskStatus.POSTPONED.name -> text = "${context.getString(R.string.reason_postponed)} "
                TaskStatus.CREATED.name -> text = context.getString(R.string.reason_unattempted)
                TaskStatus.BLOCKED.name -> text = context.getString(R.string.reason_blocked)
                TaskStatus.CANCELLED.name -> text = context.getString(R.string.reason_cancelled)
            }
        }

        if((data.metaDetails?.packageCounts?.total ?: 0) > 0){
            holder.packageCount.visibility = View.VISIBLE
            holder.packageCount.text = String.format(context.getString(R.string.package_count_label), data.metaDetails?.packageCounts?.total)
        }else{
            holder.packageCount.visibility = View.GONE
        }
    }

    class ShipmentHolder(view : View) : RecyclerView.ViewHolder(view){

        val refId : TextView = view.findViewById(R.id.tv_ref_id)
        val reason : TextView = view.findViewById(R.id.tv_reason)
        val view: View = view.findViewById(R.id.view)
        val packageCount: TextView = view.findViewById(R.id.tvPackageCount)
    }

}