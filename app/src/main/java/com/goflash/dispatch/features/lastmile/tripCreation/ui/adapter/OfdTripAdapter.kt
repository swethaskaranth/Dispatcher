package com.goflash.dispatch.features.lastmile.tripCreation.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.goflash.dispatch.R
import com.goflash.dispatch.api_services.SessionService
import com.goflash.dispatch.data.TripDTO
import com.goflash.dispatch.features.lastmile.tripCreation.listeners.SelectedListener
import com.goflash.dispatch.type.BagStatus
import com.goflash.dispatch.util.getDateStringFrom

class OfdTripAdapter(
    private val context: Context,
    private var list: List<TripDTO>,
    private val listener: SelectedListener
) : RecyclerView.Adapter<OfdTripAdapter.OfdTripHolder>() {

    private val grey by lazy {
        ContextCompat.getColor(context, R.color.color_c7)
    }

    private val textColor by lazy {
        ContextCompat.getColor(context, R.color.unassigned_count_text_color)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OfdTripHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.layout_trip_item_ofd, parent, false)
        return OfdTripHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: OfdTripHolder, position: Int) {
        val data = list[position]
        holder.tripId.text = "Trip ${data.tripId}"
        holder.tvTaskCount.text = data.taskCount.toString()
        holder.tvSprinter.text = data.agentName
        holder.tvSprinterComp.text = data.agentName
        holder.tvInactivity.text = "${data.tripId}"
        holder.tvDate.text = getDateStringFrom(data.updatedOn)

        when (data.status) {
            BagStatus.OUT_FOR_DELIVERY.name -> holder.tvSprinter.visibility = View.VISIBLE
            else -> holder.tvSprinterComp.visibility = View.VISIBLE
        }


        if(data.status == BagStatus.OUT_FOR_DELIVERY.name) {
            holder.tvSettleTrip.visibility = View.GONE
            holder.view.visibility = View.GONE
            holder.tvCall.visibility = View.GONE
            holder.tvCall2.visibility = View.VISIBLE
        }

        if(SessionService.selfAssignment) {
            holder.tvSettleTrip.isEnabled = false
            holder.tvSettleTrip.setTextColor(grey)
        }else{
            holder.tvSettleTrip.isEnabled = true
            holder.tvSettleTrip.setTextColor(textColor)
        }

        if(data.status == BagStatus.RECON_FINISHED.name){
            holder.tvSettleTrip.visibility = View.GONE
            holder.view.visibility = View.GONE
            holder.tvCall.visibility = View.GONE
            holder.tvCall2.visibility = View.GONE
        }

        holder.tvSettleTrip.setOnClickListener {
            listener.onShipmentSelected(position, 2)
        }
        holder.tvCall.setOnClickListener {
            listener.onCallDeliveryAgent(data.phoneNumber)
        }
        holder.tvCall2.setOnClickListener {
            listener.onCallDeliveryAgent(data.phoneNumber)
        }
        holder.v.setOnClickListener {
            listener.onShipmentSelected(position, 1)
        }

    }

    class OfdTripHolder(view: View) : RecyclerView.ViewHolder(view) {

        val v = view
        val tripId : TextView = v.findViewById(R.id.tvTripId!!)
        val tvTaskCount: TextView = v.findViewById(R.id.tvTaskCount)
        val tvSprinter: TextView = v.findViewById(R.id.tvSprinter)
        val tvSprinterComp: TextView = v.findViewById(R.id.tvSprinterComp)
        val tvSettleTrip: TextView = v.findViewById(R.id.tvSettleTrip)
        val tvInactivity: TextView = v.findViewById(R.id.tvInactivity)
        val tvDate: TextView = v.findViewById(R.id.tvDate)
        val tvCall : TextView= v.findViewById(R.id.tvCall)
        val tvCall2 : TextView= v.findViewById(R.id.tvCall2)
        val view: View = v.findViewById(R.id.view)
    }
}