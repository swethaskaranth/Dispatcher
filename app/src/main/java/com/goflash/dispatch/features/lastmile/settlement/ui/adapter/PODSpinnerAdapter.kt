package com.goflash.dispatch.features.lastmile.settlement.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.goflash.dispatch.R
import com.goflash.dispatch.data.AckForRecon

class PODSpinnerAdapter(private val list: List<AckForRecon>): BaseAdapter() {

    override fun getCount(): Int {
        return list.size
    }

    override fun getItem(position: Int): Any {
        return list[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val itemView = convertView ?: LayoutInflater.from(parent?.context).inflate(R.layout.layout_reason_spinner_item, parent, false)
        val textView = itemView.findViewById<TextView>(R.id.textReason)
        textView.text = list[position].displayId
        return itemView
    }


}