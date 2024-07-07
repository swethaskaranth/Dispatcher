package com.goflash.dispatch.features.lastmile.tripCreation.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.goflash.dispatch.R
import com.goflash.dispatch.features.lastmile.tripCreation.presenter.MergeCreatedTripPresenter
import com.goflash.dispatch.features.lastmile.tripCreation.view.MergeTripRowView

class MergeCreatedTripAdapter(
    private val context: Context,
    private val mPresenter: MergeCreatedTripPresenter
) : RecyclerView.Adapter<MergeCreatedTripAdapter.MergeCreatedTripHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MergeCreatedTripHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_trip_to_merge, parent, false)
        return MergeCreatedTripHolder(view, mPresenter)
    }

    override fun onBindViewHolder(holder: MergeCreatedTripHolder, position: Int) {
        mPresenter.bindViewHolder(position, holder)
    }

    override fun getItemCount(): Int {
        return mPresenter.getCount()
    }

    class MergeCreatedTripHolder(view: View, val mPresenter: MergeCreatedTripPresenter) :
        RecyclerView.ViewHolder(view), MergeTripRowView {

        val tripId: TextView = view.findViewById(R.id.tvTripId)
        val selectTrip: RadioButton = view.findViewById(R.id.rbSelect)
        val sprinter: TextView = view.findViewById(R.id.tvSprinter)
        val count : TextView = view.findViewById(R.id.tvShipmentCount)

        override fun setTripId(id: String) {
            tripId.text = id
        }

        override fun setSprinterName(name: String?) {
            if(name == null){
                sprinter.visibility = View.GONE
            }else {
                sprinter.visibility = View.VISIBLE
                sprinter.text = name
            }
        }

        override fun setCount(shipmentCount: Long) {
            count.text = "$shipmentCount"
        }

        override fun setRadioOnChangeListener(position: Int) {
            selectTrip.setOnCheckedChangeListener { compoundButton, isChecked ->
                if(isChecked)
                    mPresenter.onTripSelected(position)
            }
        }

        override fun setRadio(check: Boolean) {
            selectTrip.isChecked = check
        }
    }
}