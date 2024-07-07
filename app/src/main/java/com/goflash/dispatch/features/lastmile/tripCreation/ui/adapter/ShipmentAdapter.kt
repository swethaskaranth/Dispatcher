package com.goflash.dispatch.features.lastmile.tripCreation.ui.adapter

import android.content.Context
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.goflash.dispatch.R
import com.goflash.dispatch.data.TaskListDTO
import com.goflash.dispatch.features.lastmile.tripCreation.listeners.DataUpdateListener
import com.goflash.dispatch.util.setSlotForTaskIfAvailable


class ShipmentAdapter(
    private val context: Context,
    private val list: List<TaskListDTO>,
    private val listener : DataUpdateListener
) : RecyclerView.Adapter<ShipmentAdapter.ShipmentHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShipmentHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.layout_shipment_item, parent, false)
        return ShipmentHolder(view)
    }

    override fun onBindViewHolder(holder: ShipmentHolder, position: Int) {

        val shipment = list[position]

        holder.tvTaskType.text = shipment.type
        holder.tvPackageId.text = if (shipment.packageId == null)
            String.format(context.getString(R.string.reference_id_label),shipment.referenceId)
        else
            String.format(context.getString(R.string.package_id_label),shipment.packageId)
       /* holder.tvAddress.text =
            shipment.name + ", " + shipment.flatNumber + "," + shipment.streetName + "," + shipment.landmark + " " + shipment.city + " " + shipment.state + " " + shipment.pincode
*/
        if(shipment.address1 != null) {
            holder.tvLabelViewDetails.visibility = View.GONE
            holder.tvAddress.visibility = View.VISIBLE
            holder.tvAddress.text =
                (shipment.name + ", " +shipment.address1 + ", " + shipment.address2 + ", " + shipment.address3 + ", " + shipment.city
                        + " " + shipment.state + " " + shipment.pincode)
        }else{
            holder.tvLabelViewDetails.visibility = View.VISIBLE
            holder.tvAddress.visibility = View.GONE
        }

        holder.tvLabelViewDetails.setOnClickListener {
            listener.onViewDetails(position, shipment)
        }


        holder.tvLbn.text = String.format(context.getString(R.string.lbn_label),shipment.lbn)
        if(!shipment.childShipments.isNullOrEmpty()){
            holder.mpsCount.visibility = View.VISIBLE
            val str = SpannableString(String.format(context.getString(R.string.no_of_boxes),shipment.childShipments.size))
            str.setSpan(UnderlineSpan(),0,str.length,0)
            holder.mpsCount.text = str
        }else
            holder.mpsCount.visibility = View.GONE

        setSlotForTaskIfAvailable(holder.timeSlot, shipment.customerDefinedSlotStartTime, shipment.customerDefinedSlotEndTime)

        holder.mpsCount.setOnClickListener {
            listener.onItemSelected(position)
        }


    }

    override fun getItemCount(): Int {
        return list.size
    }


    class ShipmentHolder(view: View) : RecyclerView.ViewHolder(view) {

        val viewForeground : RelativeLayout= view.findViewById(R.id.view_foreground)
        val tvTaskType: TextView = view.findViewById(R.id.tvTaskType)
        val tvPackageId: TextView = view.findViewById(R.id.tvPackageId)
        val tvAddress: TextView = view.findViewById(R.id.tvAddress)
        val tvLabelViewDetails: TextView = view.findViewById(R.id.labelViewDetails)
        val tvLbn: TextView = view.findViewById(R.id.tvLbn)
        val mpsCount: TextView = view.findViewById(R.id.tvBoxCount)
        val timeSlot: TextView = view.findViewById(R.id.timeSlot)

    }
}