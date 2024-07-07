package com.goflash.dispatch.features.cash.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.goflash.dispatch.R
import com.goflash.dispatch.data.AdhocCashCollectionBreakup
import com.goflash.dispatch.util.getDecimalFormat
import com.goflash.dispatch.util.getTimeFromISODate

class AdhocCashCollectionBreakupAdapter(
    private val context: Context,
    private val list: MutableList<AdhocCashCollectionBreakup>
) : RecyclerView.Adapter<AdhocCashCollectionBreakupAdapter.AdhocBreakupHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdhocBreakupHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_cash_breakup_item,parent,false)
        return AdhocBreakupHolder(view)
    }

    override fun onBindViewHolder(holder: AdhocBreakupHolder, position: Int) {
        val item = list[position]

        holder.tvTripId.text = String.format(context.getString(R.string.transaction_id),item.transactionId)
        holder.tvSprinter.text = item.depositor
        holder.tvAmount.text = String.format(context.getString(R.string.cash_in_hand_text_formatted),
            getDecimalFormat(item.amount)
        )

        holder.tvReconTime.text = String.format(context.getString(R.string.recon_time),getTimeFromISODate(item.createdOn).replace("AM", "am").replace("PM","pm"))



    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun add(cashBreakup: AdhocCashCollectionBreakup){
        list.add(cashBreakup)
        notifyItemInserted(list.size -1)
    }

    fun addAll(cashList: List<AdhocCashCollectionBreakup>){
        cashList.forEach{
            add(it)
        }
    }

    class AdhocBreakupHolder(view: View) : RecyclerView.ViewHolder(view) {

        val tvTripId: TextView = view.findViewById(R.id.tvTripId)
        val tvSprinter: TextView = view.findViewById(R.id.tvSprinter)
        val tvReconTime: TextView = view.findViewById(R.id.tvReconTime)
        val tvAmount: TextView = view.findViewById(R.id.tvAmount)

    }
}