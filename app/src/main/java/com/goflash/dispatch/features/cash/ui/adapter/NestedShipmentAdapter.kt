package com.goflash.dispatch.features.cash.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.goflash.dispatch.R
import com.goflash.dispatch.data.CashDetails
import com.goflash.dispatch.features.cash.ui.listener.OnShipmentSelectedListener
import com.goflash.dispatch.util.getDecimalFormat

class NestedShipmentAdapter(
    private val context: Context,
    private val lot: CashDetails,
    private val listener: OnShipmentSelectedListener
) : RecyclerView.Adapter<NestedShipmentAdapter.NestedShipmentHolder>() {

    private var bind = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NestedShipmentHolder {
        val view =
            LayoutInflater.from(context)
                .inflate(R.layout.layout_cash_expand, parent, false)
        return NestedShipmentHolder(view)
    }

    override fun getItemCount(): Int {
        return 1
    }

    override fun onBindViewHolder(holder: NestedShipmentHolder, position: Int) {
        bind = true

        holder.tvOpeningBal.text =
            "${context.getString(R.string.Rs)} ${getDecimalFormat(lot.openingBalance.toLong())}"
        holder.tvCashCollected.text =
            "${context.getString(R.string.Rs)} ${getDecimalFormat(lot.cashCollected.toLong())}"
        holder.tvExpenses.text =
            "- ${context.getString(R.string.Rs)} ${getDecimalFormat(lot.expenses.toLong())}"
        holder.tvAmountCollected.text =
            "- ${context.getString(R.string.Rs)} ${getDecimalFormat(lot.cashDeposit.toLong())}"

        if(lot.cashCollected == 0)
            holder.labelCashCollected.setTextColor(ContextCompat.getColor(context,R.color.text_color_blue_dark))

        holder.labelCashCollected.setOnClickListener {
            if (lot.cashCollected > 0)
                listener.showCashBreakup(lot.id.toString(), holder.tvCashCollected.text.toString())
        }

        bind = false
    }


    class NestedShipmentHolder(view: View) : RecyclerView.ViewHolder(view) {

        val tvOpeningBal: TextView = view.findViewById(R.id.tv_opening_bal)
        val tvCashCollected: TextView = view.findViewById(R.id.tv_cash_collected)
        val tvExpenses: TextView = view.findViewById(R.id.tv_expenses)
        val tvAmountCollected: TextView = view.findViewById(R.id.tv_amount_collected)
        val labelCashCollected: TextView = view.findViewById(R.id.labelCashCollected)

    }
}