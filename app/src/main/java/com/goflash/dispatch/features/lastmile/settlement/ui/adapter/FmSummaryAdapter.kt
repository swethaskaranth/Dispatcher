package com.goflash.dispatch.features.lastmile.settlement.ui.adapter

import android.content.Context
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.goflash.dispatch.R
import com.goflash.dispatch.data.FmPickedShipment
import com.goflash.dispatch.features.lastmile.tripCreation.listeners.OnItemSelctedListener

class FmSummaryAdapter(
    private val context: Context, private val list: Map<String, List<FmPickedShipment>>,
    private val listener: OnItemSelctedListener,
    private val keys: List<String> = ArrayList(list.keys)
) : RecyclerView.Adapter<FmSummaryAdapter.FmSummaryHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FmSummaryHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.layout_fm_summary_item, parent, false)
        return FmSummaryHolder(view)
    }

    override fun onBindViewHolder(holder: FmSummaryHolder, position: Int) {
        val data = list[keys[position]]

        holder.originName.text = keys[position]
        holder.tvReceived.text = "${data?.size}"
        val pickedCount = data?.filter { it.isScanned }?.size?.plus(data.filter { it.reason != null }.size)
        holder.tvPicked.text = "$pickedCount"

        if (data?.any { !it.isScanned && it.reason == null } == true) {
            val str = SpannableString(context.getString(R.string.review))
            str.setSpan(UnderlineSpan(), 0, str.length, 0)
            holder.tvReview.text = str
            holder.tvReview.setTextColor(ContextCompat.getColor(context, R.color.link_blue))
            holder.tvReview.setOnClickListener { listener.onShipmentSelected(keys[position]) }
        } else {
            holder.tvReview.text = context.getString(R.string.reviewed)
            holder.tvReview.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.unassigned_text_color
                )
            )
        }


    }

    override fun getItemCount(): Int {
        return keys.size
    }


    class FmSummaryHolder(view: View) : RecyclerView.ViewHolder(view) {

        val originName: TextView = view.findViewById(R.id.originName)
        val tvReceived: TextView = view.findViewById(R.id.tvReceived)
        val tvPicked: TextView = view.findViewById(R.id.tvPicked)

        val tvReview: TextView = view.findViewById(R.id.tvReview)

    }
}