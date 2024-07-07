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
import com.goflash.dispatch.data.UndeliveredShipmentDTO
import com.goflash.dispatch.features.lastmile.settlement.listeners.SelectedListener

class UnScannedItemAdapter(
    private val context: Context,
    private val list: List<UndeliveredShipmentDTO>,
    private val listener: SelectedListener
) :
    RecyclerView.Adapter<UnScannedItemAdapter.ReviewItemHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewItemHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.items_undelivered_scanned, parent, false)
        return ReviewItemHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewItemHolder, position: Int) {
        val data = list[position]

        holder.refId.text = data.referenceId.toString()
        holder.lbn.text = data.lbn

        var returnReason: String? = null

        holder.rbDamaged.isChecked = false
        holder.rbLost.isChecked = false

        holder.rgSelectReason.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbDamaged ->{
                    returnReason = holder.rbDamaged.text.toString()

                }
                R.id.rbLost -> {returnReason = holder.rbLost.text.toString()}
            }

            data.reason = returnReason
            listener.onShipmentSelected(position, data)
        }


    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ReviewItemHolder(view: View) : RecyclerView.ViewHolder(view) {

        val refId: TextView = view.findViewById(R.id.tvRefId)
        val lbn: TextView = view.findViewById(R.id.tvLbn)
        val rgSelectReason: RadioGroup = view.findViewById(R.id.rgSelectReason)

        val rbLost: RadioButton = view.findViewById(R.id.rbLost)
        val rbDamaged: RadioButton = view.findViewById(R.id.rbDamaged)


    }
}