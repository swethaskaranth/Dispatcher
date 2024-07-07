package com.goflash.dispatch.features.dispatch.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.goflash.dispatch.R
import com.goflash.dispatch.features.dispatch.presenter.TripListPresenter
import com.goflash.dispatch.features.dispatch.view.TripRowView

class TripAdapter(private val context: Context, private val tripListPresenter: TripListPresenter) : androidx.recyclerview.widget.RecyclerView.Adapter<TripAdapter.TripHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_trip_layout, parent, false)
        return TripHolder(view)
    }

    override fun onBindViewHolder(holder: TripHolder, position: Int) {
        tripListPresenter.onBindTripRowView(position, holder)
    }

    override fun getItemCount(): Int {
        return tripListPresenter.getCount()
    }


    class TripHolder(v: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(v), TripRowView {

        val tripDate = v.findViewById<TextView>(R.id.tv_tripDate)
        val tripId = v.findViewById<TextView>(R.id.tv_trip_id)
        val tripStatus = v.findViewById<TextView>(R.id.tv_trip_status)
        val tripDestination = v.findViewById<TextView>(R.id.tv_dc_name)
        val sprinterName = v.findViewById<TextView>(R.id.tv_sprinter_name)

        override fun setTripDate(date: String) {
            tripDate.text = date
        }

        override fun setTripId(id: String) {
            tripId.text = id
        }

        override fun setTripDestination(destination: String) {
            tripDestination.text = destination
        }

        override fun setSprinterName(name: String) {
            sprinterName.text = name
        }

        override fun setTripStatus(status: String) {
            tripStatus.text = status
        }


    }
}