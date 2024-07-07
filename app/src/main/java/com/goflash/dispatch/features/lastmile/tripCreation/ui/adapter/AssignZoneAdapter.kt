package com.goflash.dispatch.features.lastmile.tripCreation.ui.adapter

import android.content.Context
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.goflash.dispatch.R
import com.goflash.dispatch.features.lastmile.tripCreation.listeners.ZoneItemListener
import com.goflash.dispatch.model.ZoneSprinterDTO
import io.github.douglasjunior.androidSimpleTooltip.SimpleTooltip

class AssignZoneAdapter(
    private val context: Context,
    private val list: List<ZoneSprinterDTO>,
    private val listener: ZoneItemListener

): RecyclerView.Adapter<AssignZoneAdapter.AssignZoneHolder>() {

    private var enableCheckbox = false

    private var bind = false

    private var tooltip: SimpleTooltip? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssignZoneHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_item_zone, parent, false)
        return AssignZoneHolder(view)
    }

    override fun onBindViewHolder(holder: AssignZoneHolder, position: Int) {

        bind = true

        val zone = list[position]
        holder.zone.text = zone.zoneList.map { it.zoneName }.joinToString(", ")
        holder.tvTaskCount.text =
            "${zone.zoneList.map { it.shipmentCount }.reduce { acc, i -> acc.plus(i) }}"

        holder.checkbox.visibility = if (enableCheckbox && !zone.zoneList.any { it.tripCreationInProgress }) View.VISIBLE else View.GONE

        holder.checkbox.isChecked = zone.zoneList.any { it.selected }

        if (zone.zoneList.size > 1  && !zone.zoneList.any { it.tripCreationInProgress }) {
            holder.demerge.visibility = View.VISIBLE
           // holder.info.visibility = View.VISIBLE
        } else {
            holder.demerge.visibility = View.GONE
           // holder.info.visibility = View.GONE
        }

        val count = zone.zoneList.map { it.priorityCount }.reduce { acc, i -> acc.plus(i) }

        if(count > 0 ){
            holder.ivHighPriority.visibility = View.VISIBLE
        }


        holder.ivHighPriority.setOnClickListener { showToolTip("High priority (${count})", position, holder) }

        with(holder.rvSprinter) {

            layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            adapter = AssignSprinterAdapter(context, zone.zoneList[0].id, zone.sprinterList, listener)
        }

        if(!zone.zoneList.any { it.tripCreationInProgress }) {
            holder.searchSprinter.setBackgroundResource(R.drawable.spinner_in_focus_border)
            holder.cancel.visibility = View.GONE

            holder.rvSprinter.setOnTouchListener { v, event ->
                //Log.d("Event", "$event")
                if (event.action == MotionEvent.ACTION_DOWN)
                    listener.onItemSelected(zone.zoneList[0].id)
                false
            }

            holder.rvSprinter.setOnClickListener {
                listener.onItemSelected(zone.zoneList[0].id)
            }

            holder.searchSprinter.setOnClickListener {
                listener.onItemSelected(zone.zoneList[0].id)
            }

            holder.checkbox.setOnCheckedChangeListener { v, isChecked ->
                if (!bind) {
                    if (isChecked) {
                        zone.zoneList.map { it.selected = true }
                        listener.addToMergeList(zone)
                    } else {
                        zone.zoneList.map { it.selected = false }
                        listener.removeFromMergeList(zone)
                    }
                }
            }

            holder.demerge.setOnClickListener {
                listener.deMergeZones(zone)
            }
        }else{
            holder.searchSprinter.setBackgroundResource(R.drawable.spinner_disabled_background)
            holder.cancel.visibility = View.VISIBLE

            holder.cancel.setOnClickListener {
                listener.onCancelTrip(zone.zoneList[0].tripProcessId)
            }
        }

        if (zone.sprinterList.isNotEmpty()) {
            holder.searchSprinter.visibility = View.GONE
            holder.rvSprinter.visibility = View.VISIBLE
        } else {
            holder.searchSprinter.visibility = View.VISIBLE
            holder.rvSprinter.visibility = View.GONE
        }


        bind = false

    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun showOrHideCheckbox(visible: Boolean) {
        enableCheckbox = visible
        notifyDataSetChanged()
    }

    private fun showToolTip(count: String, position: Int, viewHolder: AssignZoneHolder) {
        /*   if (tooltipPosition != position) {
               tooltipPosition = position*/
        tooltip?.dismiss()
        tooltip = SimpleTooltip.Builder(context)
            .anchorView(viewHolder.ivHighPriority)
            .text(count)
            .gravity(Gravity.BOTTOM)
            .backgroundColor(ContextCompat.getColor(context, R.color.text_color_blue))
            .textColor(ContextCompat.getColor(context, R.color.tooltip_text))
            .arrowColor(ContextCompat.getColor(context, R.color.text_color_blue))
            .arrowWidth(20f)
            .arrowHeight(10f)
            .build()
        tooltip?.show()
        // notifyDataSetChanged()
        // }
    }

    class AssignZoneHolder(view: View) : RecyclerView.ViewHolder(view) {

        val zone: TextView = view.findViewById(R.id.tvZone)
        val rvSprinter: RecyclerView = view.findViewById(R.id.rvSprinter)
        val checkbox: AppCompatCheckBox = view.findViewById(R.id.cbSelect)
        val tvTaskCount: TextView = view.findViewById(R.id.tvTaskCount)
        val searchSprinter: TextView = view.findViewById(R.id.searchSprinter)
        val ivHighPriority: ImageView = view.findViewById(R.id.ivHighPriority)

        val demerge: TextView = view.findViewById(R.id.tvDemerge)
        //val info: ImageView = view.ivInfo
        val cancel: TextView = view.findViewById(R.id.tvCancel)

    }
}