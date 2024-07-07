package com.goflash.dispatch.features.lastmile.settlement.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.goflash.dispatch.R
import com.goflash.dispatch.data.LostDamagedShipment

class UndeliveredMPSShipmentsAdapter(
    private val context: Context,
    private val list: List<LostDamagedShipment>) :
    RecyclerView.Adapter<UndeliveredMPSShipmentsAdapter.UndeliveredItemHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UndeliveredItemHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.items_undelivered_scanned, parent, false)
        return UndeliveredItemHolder(view)
    }

    override fun onBindViewHolder(holder: UndeliveredItemHolder, position: Int) {
        val data = list[position]

        holder.refId.text = data.referenceId.toString()
        holder.lbn.text = data.lbn
        holder.label.visibility = View.GONE

        when(data.status){
            "LOST_IN_TRANSIT" -> {
                holder.rbLost.isChecked = true
                holder.rbLost.isEnabled = false
                holder.rbLost.visibility = View.VISIBLE
                holder.rbDamaged.visibility = View.GONE
            }
            "DAMAGED_BY_SPRINTER" -> {
                holder.rbDamaged.isChecked = true
                holder.rbDamaged.isEnabled = false
                holder.rbLost.visibility = View.GONE
                holder.rbDamaged.visibility = View.VISIBLE
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class UndeliveredItemHolder(view: View) : RecyclerView.ViewHolder(view) {

        val refId: TextView = view.findViewById(R.id.tvRefId)
        val lbn: TextView = view.findViewById(R.id.tvLbn)
        val label: TextView = view.findViewById(R.id.labelSelectReason)
        val rgSelectReason: RadioGroup = view.findViewById(R.id.rgSelectReason)

        val rbLost: RadioButton = view.findViewById(R.id.rbLost)
        val rbDamaged: RadioButton = view.findViewById(R.id.rbDamaged)


    }
}