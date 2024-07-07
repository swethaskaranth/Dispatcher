package com.goflash.dispatch.features.bagging.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.goflash.dispatch.R
import com.goflash.dispatch.data.ScannedOrder

class ScannedOrdersAdapter(private val context: Context, private val list: List<ScannedOrder>) :
    RecyclerView.Adapter<ScannedOrdersAdapter.ScannedOrderHolder>() {

    companion object {
        const val TYPE_HEADER = 0
        const val TYPE_ITEM: Int = 1
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ScannedOrderHolder {

        val view =
            LayoutInflater.from(context).inflate(R.layout.scanned_order_item, parent, false)
        return ScannedOrderHolder(view)

    }

    override fun onBindViewHolder(holder: ScannedOrderHolder, position: Int) {

            val item = list[position]

            holder.refId.text = item.scannedBarcode
            holder.binNumber.text = item.binNumber

    }

    override fun getItemCount(): Int = list.size

    override fun getItemViewType(position: Int): Int =
        if (position == 0)
            TYPE_HEADER
        else TYPE_ITEM


    class ScannedOrderHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val refId: TextView = itemView.findViewById(R.id.txtRefId)
        val binNumber: TextView = itemView.findViewById(R.id.txtBinNumber)
    }

    class ScannedOrderHeader(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }

    private fun startTextAnimation(textview: TextView) {

        // Start from 0.1f if you desire 90% fade animation
        // Start from 0.1f if you desire 90% fade animation
        val fadeIn: Animation = AlphaAnimation(0.0f, 1.0f)
        fadeIn.duration = 2000
        fadeIn.startOffset = 200


        textview.startAnimation(fadeIn)

    }
}