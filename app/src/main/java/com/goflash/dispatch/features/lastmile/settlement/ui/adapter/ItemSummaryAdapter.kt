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
import com.goflash.dispatch.data.Item
import com.goflash.dispatch.features.lastmile.tripCreation.listeners.OnItemSelctedListener

class ItemSummaryAdapter(
    private val context: Context,
    private val items: List<Item>,
    private val listener: OnItemSelctedListener
) : RecyclerView.Adapter<ItemSummaryAdapter.ItemSummaryHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemSummaryHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.layout_barcoded_received_item, parent, false)
        return ItemSummaryHolder(view)
    }

    override fun onBindViewHolder(holder: ItemSummaryHolder, position: Int) {
        val item = items[position]

        holder.tvName.text = item.displayName
        holder.tvBatch.text =
            String.format(context.getString(R.string.batch_number), item.batchNumber)

        holder.tvReturnQuanityRaised.text = String.format(
            context.getString(R.string.return_request_quantity),
            item.returnRaisedQuantity
        )

        holder.tvPickedQuantity.text =
            String.format(context.getString(R.string.picked_quantity), item.returnedQuantity)

        holder.tvReject.text = "${item.reconRejectedQuantity}"

        holder.tvAccept.text = "${item.reconAcceptedQuantity}"

        if(item.reconRejectedQuantity + item.reconAcceptedQuantity != item.returnedQuantity) {
            val str = SpannableString(context.getString(R.string.review))
            str.setSpan(UnderlineSpan(),0,str.length,0)
            holder.tvReview.text = str
            holder.tvReview.setTextColor(ContextCompat.getColor(context,R.color.link_blue))
            holder.tvReview.setOnClickListener { listener.onItemSelected(position) }
        }
        else {
            holder.tvReview.text = context.getString(R.string.reviewed)
            holder.tvReview.setTextColor(ContextCompat.getColor(context,R.color.unassigned_text_color))
        }


    }


    override fun getItemCount(): Int {
        return items.size
    }


    class ItemSummaryHolder(view: View) : RecyclerView.ViewHolder(view) {

        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvBatch: TextView = view.findViewById(R.id.tvBatch)
        val tvReturnQuanityRaised: TextView = view.findViewById(R.id.tvReturnQuanityRaised)
        val tvPickedQuantity: TextView = view.findViewById(R.id.tvPickedQuantity)
        val tvReject : TextView = view.findViewById(R.id.tvReject)
        val tvAccept : TextView = view.findViewById(R.id.tvAccept)
        val tvReview : TextView = view.findViewById(R.id.tvReview)

    }
}