package com.goflash.dispatch.features.lastmile.tripCreation.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.goflash.dispatch.R
import com.goflash.dispatch.data.SprinterForZone
import com.goflash.dispatch.features.lastmile.tripCreation.listeners.ZoneSprinterListener

class ZoneSprinterAdapter(
    private val context: Context,
    private val sprinterList: List<SprinterForZone>,
    private val listener: ZoneSprinterListener,
    private val selectedList: List<SprinterForZone>
) : RecyclerView.Adapter<ZoneSprinterAdapter.ZoneSprinterHolder>() {

    private var clearAll = false

    private var onBind = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ZoneSprinterHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_sprinter_zone, parent, false)
        return ZoneSprinterHolder(view)
    }

    override fun onBindViewHolder(holder: ZoneSprinterHolder, position: Int) {
        onBind = true

        val sprinter = sprinterList[position]

        holder.name.text = sprinter.name

        if(!sprinter.isRestricted) {
            holder.checkbox.isChecked = selectedList.contains(sprinter)
            holder.checkbox.setOnCheckedChangeListener { v, isChecked ->
                if (!onBind) {
                    if (isChecked) {
                        listener.addSprinterToList(sprinter)
                    } else {
                        listener.removeSprinterFromList(sprinter)
                    }
                }
            }
            holder.itemView.setBackgroundColor(
                ContextCompat.getColor(
                    context,
                    R.color.white
                )
            )
            holder.tvRestrictionReason.visibility = View.GONE
        }else{
            holder.checkbox.isEnabled = false
            holder.tvRestrictionReason.visibility = View.VISIBLE
            holder.tvRestrictionReason.text = sprinter.restrictionReason?: "Pending Outstanding Amount"
            holder.itemView.setBackgroundColor(
                ContextCompat.getColor(
                    context,
                    R.color.disable_button
                )
            )
        }

        if (sprinter.isDisabled)
            holder.checkbox.isEnabled = false


        onBind = false
    }

    override fun getItemCount(): Int {
        return sprinterList.size
    }


    class ZoneSprinterHolder(view: View) : RecyclerView.ViewHolder(view) {

        val checkbox: CheckBox = view.findViewById(R.id.checkBox)
        val name: TextView = view.findViewById(R.id.tvName)
        val tvRestrictionReason: TextView = view.findViewById(R.id.tvRestrictionReason)

    }
}