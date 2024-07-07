package com.goflash.dispatch.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.goflash.dispatch.R
import com.goflash.dispatch.features.lastmile.tripCreation.listeners.InTransitTripListener
import com.goflash.dispatch.model.ShipmentDTO


class ChildShipmentAdapter(
    val context: Context,
    val list: List<ShipmentDTO>,
    private val listener: InTransitTripListener
) : RecyclerView.Adapter<ChildShipmentAdapter.ChildShipmentHolder>() {

    private var bind = false

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ChildShipmentAdapter.ChildShipmentHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_box_item, parent, false)
        return ChildShipmentHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ChildShipmentAdapter.ChildShipmentHolder, position: Int) {

        bind = true

        val lot = list[position]

        holder.refId.text = lot.referenceId
        holder.lbn.text = lot.lbn

        holder.cbSelect.isChecked = lot.selected

        holder.cbSelect.setOnCheckedChangeListener { v, isChecked ->
            if (!bind) {
                listener.onSelectOrDeselectItem(isChecked, position)
            }

        }


        bind = false

    }


    class ChildShipmentHolder(val view: View) : RecyclerView.ViewHolder(view) {

        val refId: TextView = view.findViewById(R.id.tvRefId)
        val lbn: TextView = view.findViewById(R.id.tvLBN)
        val cbSelect: CheckBox = view.findViewById(R.id.cbSelectShipment)

    }

}