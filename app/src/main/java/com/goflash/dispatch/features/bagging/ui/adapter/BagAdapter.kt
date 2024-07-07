package com.goflash.dispatch.features.bagging.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.goflash.dispatch.R
import com.goflash.dispatch.features.bagging.presenter.BagListPresenter
import com.goflash.dispatch.features.bagging.view.BagRowView

class BagAdapter(private val context : Context, private val bagListPresenter: BagListPresenter) : androidx.recyclerview.widget.RecyclerView.Adapter<BagAdapter.BagHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BagHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_bag_layout,parent,false)
        return BagHolder(view, bagListPresenter)
    }

    override fun onBindViewHolder(holder: BagHolder, position: Int) {
        bagListPresenter.OnBindBagRowView(position,holder)
    }

    override fun getItemCount(): Int {
        return bagListPresenter.getCount()
    }


    class BagHolder(v : View, private val presenter : BagListPresenter) : androidx.recyclerview.widget.RecyclerView.ViewHolder(v) , BagRowView {

        val view = v
        val bag_id = v.findViewById<TextView>(R.id.tv_bag_id)
        val destination_name = v.findViewById<TextView>(R.id.tv_dc_name)
        val bagStatus = v.findViewById<TextView>(R.id.bag_status)


        override fun setBagId(bagId: String) {
            bag_id.text = bagId
        }

        override fun setDestination(destination: String) {
            destination_name.text = destination
        }

        override fun setOnCLickListeners() {
            view.setOnClickListener {
                presenter.onBagItemClicked(adapterPosition)
            }
        }

        override fun setTextColor(color : Int) {
            bagStatus.setTextColor(color)
        }

        override fun setBagStatus(status: String) {
            bagStatus.text = status
        }


    }
}