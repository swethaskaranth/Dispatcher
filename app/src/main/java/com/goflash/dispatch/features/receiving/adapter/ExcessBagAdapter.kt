package com.goflash.dispatch.features.receiving.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import co.uk.rushorm.core.RushSearch
import com.goflash.dispatch.R
import com.goflash.dispatch.app_constants.BAGID
import com.goflash.dispatch.app_constants.TRIPID
import com.goflash.dispatch.app_constants.VEHICLEID
import com.goflash.dispatch.data.VehicleDetails

class ExcessBagAdapter(private val context: Context, val items: MutableList<VehicleDetails>) :
    androidx.recyclerview.widget.RecyclerView.Adapter<ExcessBagAdapter.ReceivingHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReceivingHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_excess, parent, false)
        return ReceivingHolder(view)
    }

    override fun onBindViewHolder(holder: ReceivingHolder, position: Int) {
        val lot = items[position]

        holder.txtBagId.text = lot.bagId


        // doAsync {
        val vDetails = if(lot.vehicleId != null)
            RushSearch().whereEqual(VEHICLEID, lot.vehicleId).and().whereEqual(BAGID, lot.bagId)
            .findSingle(VehicleDetails::class.java)
        else
            RushSearch().whereEqual(TRIPID, lot.tripId).and().whereEqual(BAGID, lot.bagId)
                .findSingle(VehicleDetails::class.java)
        vDetails.returnReason = context.getString(R.string.excess_bag)
        vDetails.save()
        //           }


    }

    override fun getItemCount(): Int {
        return items.size
    }

    class ReceivingHolder(v: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(v) {
        val view: View = v
        val txtBagId = view.findViewById<TextView>(R.id.txt_bagId)
    }
}