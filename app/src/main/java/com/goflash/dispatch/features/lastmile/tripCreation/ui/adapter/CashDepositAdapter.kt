package com.goflash.dispatch.features.lastmile.tripCreation.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.goflash.dispatch.R
import com.goflash.dispatch.data.CdsCashCollection
import kotlin.math.roundToInt

class CashDepositAdapter(private val context: Context,
                         private val list: ArrayList<CdsCashCollection>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        val TYPE_HEADER = 0
        val TYPE_ITEM = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            val view = LayoutInflater.from(context).inflate(R.layout.item_transaction_header, parent, false)
            CashDepositHeader(view)
        } else {
            val view = LayoutInflater.from(context).inflate(R.layout.item_cash_transaction, parent, false)
            CashDepositHolder(view)
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is CashDepositHolder) {
            val transaction = getItem(position)
            holder.transactionId.text = transaction.partnerTransactionId
            holder.amount.text = String.format(context.getString(R.string.cash_in_hand_text), transaction.amount.roundToInt())

        }
    }

    override fun getItemCount(): Int {
        return list.size.plus(1)
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0)
            TYPE_HEADER
        else
            TYPE_ITEM
    }

    private fun getItem(position : Int): CdsCashCollection {
        return list[position-1]
    }

    class CashDepositHolder(view: View) : RecyclerView.ViewHolder(view) {

        val transactionId: TextView = view.findViewById(R.id.tvTransactionId)
        val amount: TextView = view.findViewById(R.id.tvAmount)
    }

    class CashDepositHeader(view: View) : RecyclerView.ViewHolder(view) {

    }
}