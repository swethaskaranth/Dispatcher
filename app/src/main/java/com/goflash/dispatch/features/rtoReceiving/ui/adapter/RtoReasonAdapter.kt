package com.goflash.dispatch.features.rtoReceiving.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import com.goflash.dispatch.R
import com.goflash.dispatch.features.rtoReceiving.listeners.ReasonSelectionListener

class RtoReasonAdapter(
    private val context: Context,
    private val reasons: List<String>,
    private val listener: ReasonSelectionListener
) : RecyclerView.Adapter<RtoReasonAdapter.ReasonHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReasonHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.lauout_rto_reason_item, parent, false)
        return ReasonHolder((view))
    }

    override fun onBindViewHolder(holder: ReasonHolder, position: Int) {

        val reason = reasons[position]
        holder.cbReason.text = reason
        holder.cbReason.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked)
                listener.onReasonSelected(position)
            else
                listener.onReasonUnselected(position)
        }
    }

    override fun getItemCount(): Int {
        return reasons.size
    }

    class ReasonHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cbReason: CheckBox = view.findViewById(R.id.cbReason)

    }
}