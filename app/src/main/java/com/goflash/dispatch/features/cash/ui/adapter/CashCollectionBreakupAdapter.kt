package com.goflash.dispatch.features.cash.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.goflash.dispatch.R
import com.goflash.dispatch.data.CashCollectionTripDetails
import com.goflash.dispatch.util.getDecimalFormat
import com.goflash.dispatch.util.getTimeFromISODate

class CashCollectionBreakupAdapter(
    private val context: Context,
    private val list: MutableList<CashCollectionTripDetails>
) : RecyclerView.Adapter<CashCollectionBreakupAdapter.CashBreakupHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CashBreakupHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_cash_breakup_item,parent,false)
        return CashBreakupHolder(view)
    }

    override fun onBindViewHolder(holder: CashBreakupHolder, position: Int) {
        val item = list[position]

        holder.tvTripId.text = String.format(context.getString(R.string.trip_number),item.tripId)
        holder.tvSprinter.text = item.sprinterName
        holder.tvAmount.text = String.format(context.getString(R.string.cash_in_hand_text_formatted),
            getDecimalFormat(item.actualCashAmountCollected)
        )

        holder.tvReconTime.text = String.format(context.getString(R.string.recon_time),getTimeFromISODate(item.reconDateTime).replace("AM", "am").replace("PM","pm"))



    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun add(cashBreakup: CashCollectionTripDetails){
        list.add(cashBreakup)
        notifyItemInserted(list.size -1)
    }

    fun addAll(cashList: List<CashCollectionTripDetails>){
        cashList.forEach{
            add(it)
        }
    }

    class CashBreakupHolder(view: View) : RecyclerView.ViewHolder(view) {

        val tvTripId: TextView = view.findViewById(R.id.tvTripId)
        val tvSprinter: TextView = view.findViewById(R.id.tvSprinter)
        val tvReconTime: TextView = view.findViewById(R.id.tvReconTime)
        val tvAmount: TextView = view.findViewById(R.id.tvAmount)

    }
}