package com.goflash.dispatch.features.lastmile.tripCreation.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.goflash.dispatch.R
import com.goflash.dispatch.data.InTransitTrip
import com.goflash.dispatch.features.lastmile.tripCreation.listeners.InTransitTripListener
import java.text.SimpleDateFormat
import java.util.*

class InTransitTripAdapter(
    private val context: Context,
    private val list: ArrayList<InTransitTrip>,
    private val listener: InTransitTripListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var bind = false

    companion object {
        val HEADER = 0
        val ITEM = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == HEADER) {
            val view =
                LayoutInflater.from(context).inflate(R.layout.item_in_transit_header, parent, false)
            return InTransitTripHeader(view)
        } else {
            val view =
                LayoutInflater.from(context).inflate(R.layout.item_in_transit_trip, parent, false)
            return InTransitTripHolder(view)
        }

    }

    override fun getItemCount(): Int {
        return list.size.plus(1)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        bind = true
        if (position == 0) {
            (holder as InTransitTripHeader).cbSelectAll.isChecked =
                list.any { !it.selected } != true

            holder.cbSelectAll.setOnCheckedChangeListener { v, isChecked ->
                if (!bind) {
                   listener.onSelectOrDeselectAll(isChecked)
                }

            }

        } else {
            val item = list[position - 1]
            (holder as InTransitTripHolder).tvTripId.text = "${item.id}"
            holder.tvSprinter.text = String.format(context.getString(R.string.in_transit_asset_and_sprinter),item.asset,item.agentName)
            holder.tvDispatchTime.text = context.getString(R.string.dispatch_time,getTimeFromISODate(item.createdOn))
            holder.count.text = "${item.shipmentCount}"

            holder.cbSelect.isChecked = item.selected
            holder.cbSelect.setOnCheckedChangeListener { v, isChecked ->
                if (!bind) {
                   listener.onSelectOrDeselectItem(isChecked,position-1)

                }
            }
        }
        bind = false
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0)
            HEADER
        else
            ITEM

    }


    class InTransitTripHolder(view: View) : RecyclerView.ViewHolder(view) {

        val tvTripId: TextView = view.findViewById(R.id.tvTripId)
        val tvSprinter: TextView = view.findViewById(R.id.tvSprinter)
        val tvDispatchTime: TextView = view.findViewById(R.id.tvDispatchTime)
        val count: TextView = view.findViewById(R.id.tvCount)

        val cbSelect: CheckBox = view.findViewById(R.id.cbSelect)
    }

    class InTransitTripHeader(view: View) : RecyclerView.ViewHolder(view) {

        val cbSelectAll: CheckBox = view.findViewById(R.id.cbSelectAll)
    }

    private fun getTimeFromISODate(dateStr: String): String {

        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        val cal = Calendar.getInstance()
        // format.timeZone = TimeZone.getTimeZone("IST")
        cal.time = format.parse(dateStr)


        val dateFormat = SimpleDateFormat("dd-MM-yy | hh.mm a")
        val dateforrow = dateFormat.format(cal.time)

        return dateforrow

    }


}