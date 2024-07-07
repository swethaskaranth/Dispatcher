package com.goflash.dispatch.features.receiving.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.goflash.dispatch.R
import com.goflash.dispatch.features.dispatch.presenter.ScannedBagsPresenter
import com.goflash.dispatch.features.bagging.view.BagRowView
import com.goflash.dispatch.features.receiving.presenter.RemovedBagsPresenter

class RemoveBagsAdapter(private val context: Context, private val scannedBagsPresenter: RemovedBagsPresenter) : androidx.recyclerview.widget.RecyclerView.Adapter<RemoveBagsAdapter.RemoveBagsHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):RemoveBagsHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_scanned_bag, parent, false)
        return RemoveBagsHolder(view)
    }

    override fun getItemCount(): Int {
        return scannedBagsPresenter.getCount()
    }

    override fun onBindViewHolder(holder: RemoveBagsHolder, position: Int) {
        scannedBagsPresenter.onBindBagdRowView(position, holder)
    }


    class RemoveBagsHolder(v: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(v), BagRowView {

        val view: View = v
        val order_id: TextView = view.findViewById(R.id.order_id)

        override fun setBagId(bagId: String) {
            order_id.text = bagId
        }

    }
}