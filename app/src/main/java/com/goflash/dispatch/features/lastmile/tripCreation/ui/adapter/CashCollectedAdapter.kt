package com.goflash.dispatch.features.lastmile.tripCreation.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.goflash.dispatch.R
import com.goflash.dispatch.data.CashBreakUp

class CashCollectedAdapter (private val context: Context, private var list: List<CashBreakUp>) : RecyclerView.Adapter<CashCollectedAdapter.OfdTripHolder>(){


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OfdTripHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_cash_breakup,parent,false)
        return OfdTripHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: OfdTripHolder, position: Int) {
        val data = list[position]
        holder.tvRefId.text = data.referenceId
        holder.tvCash.text =  String.format(context.getString(R.string.cash_in_hand_text), data.amount)
        holder.tvSprinter.text = data.name
        if (data.transactionId != null)
            holder.tv_transaction_id.text = "${data.paymentType} - #${data.transactionId}"
        else
            holder.tv_transaction_id.visibility = View.GONE


    }

    class OfdTripHolder(view : View) : RecyclerView.ViewHolder(view) {

        val v = view
        val tvRefId: TextView = view.findViewById(R.id.tv_ref_id)
        val tvCash: TextView = view.findViewById(R.id.tv_cash)
        val tvSprinter: TextView = view.findViewById(R.id.tv_agent_name)
        val tv_transaction_id: TextView = view.findViewById(R.id.tv_transaction_id)
    }
}