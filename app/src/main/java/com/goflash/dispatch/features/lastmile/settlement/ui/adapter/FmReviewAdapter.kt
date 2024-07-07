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
import com.goflash.dispatch.data.FmPickedShipment
import com.goflash.dispatch.features.lastmile.settlement.listeners.SelectedListener

class FmReviewAdapter(private val context: Context, private val list: List<FmPickedShipment>, private val listener: SelectedListener): RecyclerView.Adapter<FmReviewAdapter.FmReviewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FmReviewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_review_fm_shipment_item,parent,false)
        return FmReviewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: FmReviewHolder, position: Int) {
        val shipment = list[position]

        holder.tvRefId.text = shipment.referenceId
        holder.tvLbn.text = shipment.lbn

        holder.rbDamaged.isChecked = false
        holder.rbLost.isChecked = false

        when(shipment.reason){
            "Lost in transit"-> holder.rbLost.isChecked = true
            "Damaged by DB"-> holder.rbDamaged.isChecked = true
        }

        var returnReason: String? = null

        holder.rgSelectReason.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbDamaged ->{
                    returnReason = holder.rbDamaged.text.toString()

                }
                R.id.rbLost -> {returnReason = holder.rbLost.text.toString()}
            }
            listener.onFmShipmentReasonSelected(position, returnReason!!)
        }

    }

    class FmReviewHolder(view: View): RecyclerView.ViewHolder(view){

        val tvRefId: TextView = view.findViewById(R.id.tvRefId)
        val tvLbn: TextView = view.findViewById(R.id.tvLbn)
        val rgSelectReason: RadioGroup = view.findViewById(R.id.rgSelectReason)
        val rbLost: RadioButton = view.findViewById(R.id.rbLost)
        val rbDamaged: RadioButton = view.findViewById(R.id.rbDamaged)

    }
}