package com.goflash.dispatch.features.cash.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.goflash.dispatch.R
import com.goflash.dispatch.data.CashDetails
import com.goflash.dispatch.features.cash.ui.listener.OnShipmentSelectedListener
import com.goflash.dispatch.util.getDate
import com.goflash.dispatch.util.getDecimalFormat

/**
 *Created by Ravi on 06/10/20.
 */
class CashClosingAdapter(private val context: Context,
                         private val list: MutableList<CashDetails>,
                         private val listener: OnShipmentSelectedListener
)
    : RecyclerView.Adapter<CashClosingAdapter.CashClosingHolder>(){

    private var bind = false
    private val viewPool = RecyclerView.RecycledViewPool()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CashClosingHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.layout_cash_detail, parent, false)
        return CashClosingHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: CashClosingHolder, position: Int) {
        val lot = list[position]

        holder.tvDate.text = getDate(lot.createdOn)
        holder.tvClosingId.text = lot.id.toString()
        holder.tvCashInHand.text = "${context.getString(R.string.Rs)} ${getDecimalFormat((lot.openingBalance+lot.cashCollected-lot.cashDeposit-lot.expenses).toLong())}"

        val childLayoutManager = LinearLayoutManager( holder.recyclerView.context, RecyclerView.VERTICAL, false)
        childLayoutManager.initialPrefetchItemCount = 500

        holder.recyclerView.apply {
            layoutManager = childLayoutManager
            adapter = NestedShipmentAdapter(context, list[position], listener)
            setRecycledViewPool(viewPool)
        }

        holder.ivExpand.setOnClickListener {
            if (holder.recyclerView.visibility == View.VISIBLE) {
                holder.ivExpand.rotation = holder.ivExpand.rotation + 180
                holder.recyclerView.visibility = View.GONE
                holder.llHeader.setBackgroundResource(R.drawable.border_search)
            } else {
                holder.ivExpand.rotation = holder.ivExpand.rotation + 180
                holder.recyclerView.visibility = View.VISIBLE
                holder.llHeader.setBackgroundResource(R.drawable.border_cash_back)
            }
        }
    }

    class CashClosingHolder(view: View) : RecyclerView.ViewHolder(view) {

        val recyclerView: RecyclerView = view.findViewById(R.id.rv_expand_view)
        val ivExpand: ImageView = view.findViewById(R.id.iv_expand)
        val tvDate: TextView = view.findViewById(R.id.tv_date)
        val tvClosingId: TextView = view.findViewById(R.id.tv_closing_id)
        val tvCashInHand: TextView = view.findViewById(R.id.tv_cash_in_hand)
        val llHeader: LinearLayout = view.findViewById(R.id.ll_header)
    }

}