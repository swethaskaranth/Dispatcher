package com.goflash.dispatch.features.receiving.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.goflash.dispatch.R
import com.goflash.dispatch.data.ReceivingDto
import com.goflash.dispatch.listeners.ItemSelectedListener
import com.goflash.dispatch.type.PackageStatus
import java.text.SimpleDateFormat
import java.util.*

class ReceivingAdapter(private val context : Context, private val items: MutableList<ReceivingDto>, private val itemSelectedListener: ItemSelectedListener) : RecyclerView.Adapter<ReceivingAdapter.ReceivingHolder>(){

    private val green by lazy {
        ContextCompat.getColor(context, R.color.green)
    }

    private val mdBlue by lazy {
        ContextCompat.getColor(context, R.color.md_blue)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReceivingHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_consignment_layout, parent,false)
        return ReceivingHolder(view)
    }

    override fun onBindViewHolder(holder: ReceivingHolder, position: Int) {
        val lot = items[position]

        holder.tvShipmentId.text =  lot.vehicleId?:"${lot.tripId}"
        holder.tvStatus.text     = if(lot.status == PackageStatus.RECON_FINISHED.name) "COMPLETED" else showStatus(lot.status)
        holder.tvAssetName.text  = "(${lot.assetName}) - ${lot.agentName}"
        holder.tvBagsCount.text  = "${lot.tasks.size} Bags"

        with(holder.tvStatus) {
            when (lot.status) {
                PackageStatus.RECON_FINISHED.name -> setTextColor(green)
                else -> setTextColor(mdBlue)
            }
        }

        holder.view.setOnClickListener {

           if (showStatus(lot.status) != PackageStatus.COMPLETED.name){
                itemSelectedListener.onItemSelected(lot)
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class ReceivingHolder(v : View) : RecyclerView.ViewHolder(v){
        val view: View = v
        val tvShipmentId: TextView = view.findViewById(R.id.tv_shipment_id)
        val tvStatus: TextView = view.findViewById(R.id.tv_status)
        val tvAssetName: TextView = view.findViewById(R.id.tv_assetName)
        val tvBagsCount: TextView = view.findViewById(R.id.tv_bags_count)
        val tvDispatchTime: TextView = view.findViewById(R.id.tv_dispatch_time)
    }

    private fun showStatus(status: String): String{
        return when(status){
            PackageStatus.OUT_FOR_DELIVERY.name -> "OUT FOR DELIVERY"
            PackageStatus.PROCESSED.name -> "Arrived"
            PackageStatus.COMPLETED.name -> "COMPLETED"
            else -> ""
        }
    }

    private fun getTimeFromISODate(dateStr: String): String {

        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        val cal = Calendar.getInstance()
        format.timeZone = TimeZone.getTimeZone("IST")
        cal.time = format.parse(dateStr)


        val dateFormat = SimpleDateFormat("dd-MM-yy, hh:mm a")
        val dateforrow = dateFormat.format(cal.time)

        return dateforrow

    }
}