package com.goflash.dispatch.features.lastmile.tripCreation.ui.adapter

import android.content.Context
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.goflash.dispatch.R
import com.goflash.dispatch.data.UnassignedDTO
import com.goflash.dispatch.features.lastmile.tripCreation.listeners.UnassignedShipmentListener
import com.goflash.dispatch.type.ShipmentType
import com.goflash.dispatch.util.*


class UnassignedAdapter(
    private val context: Context,
    private val list: MutableList<UnassignedDTO>,
    private val listener: UnassignedShipmentListener
) : RecyclerView.Adapter<UnassignedAdapter.UnassignedHolder>() {

    private val yellow by lazy {
        ContextCompat.getColor(context, R.color.high_priority_text_color)
    }

    private val red by lazy {
        ContextCompat.getColor(context, R.color.canc_border)
    }

    private val orange by lazy {
        ContextCompat.getColor(context, R.color.orange_light)
    }

    private val textColor by lazy {
        ContextCompat.getColor(context, R.color.unassigned_text_color)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UnassignedHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.layout_unassigned_shipment_item, parent, false)
        return UnassignedHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: UnassignedHolder, position: Int) {
        val data = list[position]
        val type = ""

        holder.tvPriority.text = data.priorityType
        if(data.shipmentType == ShipmentType.FMPICKUP.name){
            holder.tvLbn.visibility = View.GONE
            holder.tvExpected.visibility = View.VISIBLE
            val str = SpannableString("Expected ${data.fmExpectedCount} Shipments")
            str.setSpan(UnderlineSpan(),9,str.length,0)
            str.setSpan(ForegroundColorSpan(ContextCompat.getColor(context,R.color.link_blue)),9,str.length,0)
            holder.tvExpected.text = str
        }else {
            holder.tvExpected.visibility = View.GONE
            holder.tvLbn.visibility = View.VISIBLE
            holder.tvLbn.text = "LBN #${data.lbn}"
        }

        val orderId = if (data.shipmentType == ShipmentType.FMPICKUP.name)
            "Pickup Id #${data.referenceId}"
        else if (data.packageId != null && data.packageId!!.isNotEmpty())
            "Package Id #${data.packageId}"
        else "Order Id #${data.referenceId}"

        when {
            data.type == ShipmentType.RETURN.name || data.shipmentType == ShipmentType.FMPICKUP.name -> {
                holder.ivDelete.visibility = View.GONE
                holder.ivEdit.visibility = View.GONE
                if (data.priorityType == "HIGH") {
                    holder.tvTaskType.setTextColor(red)
                    holder.tvTaskType.text =
                        "Priority ${if (data.shipmentType == ShipmentType.FMPICKUP.name) ShipmentType.FMPICKUP.name else ShipmentType.Pickup.name}"
                    if (data.committedExpectedDeliveryDate != null && checkBreachTime(data.committedExpectedDeliveryDate!!)) {
                        holder.tvCommitTime.visibility = View.VISIBLE
                        holder.tvCommitTime.text =
                            "by ${getTime(data.committedExpectedDeliveryDate!!)}"
                    } else
                        holder.tvCommitTime.visibility = View.GONE
                } else {
                    holder.tvTaskType.text =
                        if (data.shipmentType == ShipmentType.FMPICKUP.name) ShipmentType.FMPICKUP.name else ShipmentType.Pickup.name
                    holder.tvCommitTime.visibility = View.GONE
                }
            }
            data.type == ShipmentType.FORWARD.name -> {
                holder.ivDelete.visibility = View.VISIBLE
                holder.ivEdit.visibility = View.GONE
                if (data.priorityType == "HIGH") {
                    holder.tvTaskType.setTextColor(red)
                    holder.tvTaskType.text = "Priority ${ShipmentType.Delivery.name}"
                    if (data.committedExpectedDeliveryDate != null && checkBreachTime(data.committedExpectedDeliveryDate!!)) {
                        holder.tvCommitTime.visibility = View.VISIBLE
                        holder.tvCommitTime.text =
                            "by ${getTime(data.committedExpectedDeliveryDate!!)}"

                    } else
                        holder.tvCommitTime.visibility = View.GONE
                } else {
                    holder.tvTaskType.setTextColor(textColor)
                    holder.tvTaskType.text = ShipmentType.Delivery.name
                    holder.tvCommitTime.visibility = View.GONE
                }
            }

        }

        if (data.address1 != null) {
            holder.tvLabelViewDetails.visibility = View.GONE
            holder.tvAddress.visibility = View.VISIBLE
            holder.tvAddress.text =
                (data.name + ", " +data.address1 + ", " + data.address2 + ", " + data.address3 + ", " + data.city
                        + " " + data.state + " " + data.pincode)
        } else {
            holder.tvLabelViewDetails.visibility = View.VISIBLE
            holder.tvAddress.visibility = View.GONE
        }

        holder.tvPackageId.text = orderId

        holder.tvPriority.text = showPriority(data, context)

        if (getEvent(data) && data.type == ShipmentType.RETURN.name) {
            holder.tvPriority.text = context.getString(R.string.postponed)
            holder.ivEdit.visibility = View.VISIBLE
        } else if (data.type == ShipmentType.FORWARD.name && getEvent(data)) {
            holder.ivEdit.visibility = View.VISIBLE
            holder.ivDelete.visibility = View.GONE
        }

        with(holder.tvPriority) {
            when (holder.tvPriority.text.toString()) {
                context.getString(R.string.blocked_braces) -> setTextColor(red)
                context.getString(R.string.postponed) -> setTextColor(orange)
            }
        }

        if(data.shipmentType == ShipmentType.MPS.name){
            holder.mpsCount.visibility = View.VISIBLE
            val str = SpannableString(String.format(context.getString(R.string.no_of_boxes),data.mpsCount))
            str.setSpan(UnderlineSpan(),0,str.length,0)
            holder.mpsCount.text = str
        }else
            holder.mpsCount.visibility = View.GONE

        setSlotForTaskIfAvailable(holder.timeSlot, data.customerDefinedSlotStartTime, data.customerDefinedSlotEndTime)

        holder.ivDelete.setOnClickListener {
            listener.onDeleteShipemnt(position, data)
        }

        holder.ivEdit.setOnClickListener {
            listener.onEditShipment(position, data)
        }

        holder.tvLabelViewDetails.setOnClickListener {
            listener.onViewDetails(position, data)
        }

        holder.mpsCount.setOnClickListener {
            listener.onMpsCountClicked(position)
        }

        if((data.shipmentMetaDetails?.packageCounts?.total ?: 0) > 0) {
            holder.tvPackageCount.visibility = View.VISIBLE
            holder.tvPackageCount.text = String.format(context.getString(R.string.package_count_label), data.shipmentMetaDetails?.packageCounts?.total)
        }else{
            holder.tvPackageCount.visibility = View.GONE
        }

    }

    fun addItem(item: String) {
        //list.add(0,item)
        notifyDataSetChanged()
    }

    class UnassignedHolder(view: View) : RecyclerView.ViewHolder(view) {

        val tvTaskType: TextView = view.findViewById(R.id.tvTaskType)
        val ivPriority: ImageView = view.findViewById(R.id.ivPrioroty)
        val tvPriority: TextView = view.findViewById(R.id.tvPriority)
        val ivDelete: ImageView = view.findViewById(R.id.ivDelete)
        val tvPackageId: TextView = view.findViewById(R.id.tvPackageId)
        val tvLbn: TextView = view.findViewById(R.id.tvLbn)
        val tvAddress: TextView = view.findViewById(R.id.tvAddress)
        val tvCommitTime: TextView = view.findViewById(R.id.tv_commit_time)
        val ivEdit: ImageView = view.findViewById(R.id.ivEdit)
        val tvLabelViewDetails: TextView = view.findViewById(R.id.labelViewDetails)
        val mpsCount: TextView = view.findViewById(R.id.tvBoxCount)
        val tvExpected: TextView = view.findViewById(R.id.tvExpected)
        val timeSlot: TextView = view.findViewById(R.id.timeSlot)
        val tvPackageCount: TextView = view.findViewById(R.id.tvPackageCount)

    }
}