package com.goflash.dispatch.features.lastmile.tripCreation.ui.adapter

import android.content.Context
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.goflash.dispatch.R
import com.goflash.dispatch.data.UnassignedDTO
import com.goflash.dispatch.features.lastmile.tripCreation.listeners.OnShipmentSelectedListener
import com.goflash.dispatch.type.ShipmentType
import com.goflash.dispatch.util.*

class NestedShipmentAdapter(
    private val context: Context,
    private val list: List<UnassignedDTO>,
    private val listener: OnShipmentSelectedListener
) : RecyclerView.Adapter<NestedShipmentAdapter.NestedShipmentHolder>() {

    private var bind = false

    private val red by lazy {
        ContextCompat.getColor(context, R.color.canc_border)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NestedShipmentHolder {
        val view =
            LayoutInflater.from(context)
                .inflate(R.layout.layout_nested_shipment_item, parent, false)
        return NestedShipmentHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(
        holder: NestedShipmentHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
            onBindViewHolder(holder, position)

    }

    override fun onBindViewHolder(holder: NestedShipmentHolder, position: Int) {
        bind = true
        val shipment = list[position]

        if (position == list.size - 1)
            holder.itemView.setBackgroundResource(R.drawable.nested_item_with_rounded_corner_background)


        when {
            shipment.type == ShipmentType.RETURN.name || shipment.shipmentType == ShipmentType.FMPICKUP.name -> {
                if (shipment.priorityType == "HIGH") {
                    holder.tvTaskType.setTextColor(red)
                    holder.tvTaskType.text = "Priority ${if(shipment.shipmentType == ShipmentType.FMPICKUP.name) ShipmentType.FMPICKUP.name else ShipmentType.Pickup.name}"

                    if (shipment.committedExpectedDeliveryDate != null && checkBreachTime(shipment.committedExpectedDeliveryDate)) {
                        holder.tvCommitTime.visibility = View.VISIBLE
                        holder.tvCommitTime.text =
                            "by ${getTime(shipment.committedExpectedDeliveryDate)}"
                    } else
                        holder.tvCommitTime.visibility = View.GONE
                } else {
                    holder.tvCommitTime.visibility = View.GONE
                    holder.tvTaskType.text = if(shipment.shipmentType == ShipmentType.FMPICKUP.name) ShipmentType.FMPICKUP.name else ShipmentType.Pickup.name
                }
            }

            shipment.type == ShipmentType.FORWARD.name -> {
                if (shipment.priorityType == "HIGH") {
                    holder.tvTaskType.setTextColor(red)
                    holder.tvTaskType.text = "Priority ${ShipmentType.Delivery.name}"

                    if (shipment.committedExpectedDeliveryDate != null && checkBreachTime(shipment.committedExpectedDeliveryDate)) {
                        holder.tvCommitTime.visibility = View.VISIBLE
                        holder.tvCommitTime.text =
                            "by ${getTime(shipment.committedExpectedDeliveryDate)}"
                    } else
                        holder.tvCommitTime.visibility = View.GONE
                } else {
                    holder.tvCommitTime.visibility = View.GONE
                    holder.tvTaskType.text = ShipmentType.Delivery.name
                }
            }

        }

        holder.tvPackageId.text = when {
            shipment.shipmentType == ShipmentType.FMPICKUP.name -> "Pickup Id #${shipment.referenceId}"
            shipment.packageId == null -> String.format(context.getString(R.string.reference_id_label), shipment.referenceId)
            else -> String.format(context.getString(R.string.package_id_label), shipment.packageId)
        }

        if(shipment.shipmentType == ShipmentType.FMPICKUP.name) {
            holder.tvExpected.visibility = View.VISIBLE
            val str = SpannableString("Expected ${shipment.fmExpectedCount} Shipments")
            str.setSpan(UnderlineSpan(),9,str.length,0)
            str.setSpan(ForegroundColorSpan(ContextCompat.getColor(context,R.color.link_blue)),9,str.length,0)
            holder.tvExpected.text = str
        }
        else
            holder.tvExpected.visibility = View.GONE


        holder.lbn.text = String.format(context.getString(R.string.lbn_label), shipment.lbn)

        if (shipment.shipmentType == ShipmentType.MPS.name) {
            holder.mpsCount.visibility = View.VISIBLE
            val str = SpannableString(
                String.format(
                    context.getString(R.string.no_of_boxes),
                    shipment.mpsCount
                )
            )
            str.setSpan(UnderlineSpan(), 0, str.length, 0)
            holder.mpsCount.text = str
        } else
            holder.mpsCount.visibility = View.GONE

        holder.mpsCount.setOnClickListener {
            val id = shipment.packageId ?: shipment.referenceId
            listener.onMpsCountClicked(shipment.shipmentId, id, shipment.mpsCount)
        }


        if (!shipment.address1.isNullOrEmpty()) {
            holder.tvAddress.visibility = View.VISIBLE
            holder.labelViewDetails.visibility = View.GONE
            holder.tvAddress.text =
                shipment.name + ", " + shipment.address1 + "," + shipment.address2 + "," + shipment.address3 + " " + shipment.city + " " + shipment.state + " " + shipment.pincode
        } else {
            holder.tvAddress.visibility = View.GONE
            holder.labelViewDetails.visibility = View.VISIBLE
        }

        setSlotForTaskIfAvailable(holder.timeSlot, shipment.customerDefinedSlotStartTime, shipment.customerDefinedSlotEndTime)


        holder.cbSelectShipment.isChecked = shipment.selected
        holder.cbSelectShipment.setOnCheckedChangeListener { v, isChecked ->
            if (!bind) {
                if (isChecked)
                    listener.onShipmentSelected(mutableListOf(shipment))
                else
                    listener.onShipmentUnselected(mutableListOf(shipment))
            }
        }

        holder.labelViewDetails.setOnClickListener {
            listener.onViewAddress(position, shipment)
        }

        if((shipment.shipmentMetaDetails?.packageCounts?.total ?: 0) > 0) {
            holder.tvPackageCount.visibility = View.VISIBLE
            holder.tvPackageCount.text = String.format(context.getString(R.string.package_count_label), shipment.shipmentMetaDetails?.packageCounts?.total)
        }else{
            holder.tvPackageCount.visibility = View.GONE
        }


        bind = false
    }

    class NestedShipmentHolder(view: View) : RecyclerView.ViewHolder(view) {

        val cbSelectShipment: CheckBox = view.findViewById(R.id.cbSelectShipment)
        val tvTaskType: TextView = view.findViewById(R.id.tvTaskType)
        val tvPackageId: TextView = view.findViewById(R.id.tvPackageId)
        val tvAddress: TextView = view.findViewById(R.id.tvAddress)
        val tvCommitTime: TextView = view.findViewById(R.id.tv_commit_time)
        val labelViewDetails: TextView = view.findViewById(R.id.labelViewDetails)
        val lbn: TextView = view.findViewById(R.id.tvLbn)
        val mpsCount: TextView = view.findViewById(R.id.tvBoxCount)
        val tvExpected: TextView = view.findViewById(R.id.tvExpected)
        val timeSlot: TextView = view.findViewById(R.id.timeSlot)
        val tvPackageCount: TextView = view.findViewById(R.id.tvPackageCount)

    }
}