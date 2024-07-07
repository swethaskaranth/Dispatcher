package com.goflash.dispatch.features.lastmile.tripCreation.ui.adapter

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.goflash.dispatch.R
import com.goflash.dispatch.data.SprinterList
import com.goflash.dispatch.features.lastmile.tripCreation.listeners.OnItemSelctedListener

class SprinterAdapter (private val context: Context, private val sprinterList : List<SprinterList>, private val listener : OnItemSelctedListener) : RecyclerView.Adapter<SprinterAdapter.SprinterHolder>(){

    private var selected = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SprinterHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_sprinter_item,parent,false)
        return SprinterHolder(
            view
        )
    }

    override fun getItemCount(): Int {
        return sprinterList.size
    }

    override fun onBindViewHolder(holder: SprinterHolder, position: Int) {
       val sprinter = sprinterList[holder.bindingAdapterPosition]

        holder.tvSprinter.text = sprinter.name

        if(!sprinter.restricted) {
            holder.tvReason.visibility = View.GONE
            if (selected == position) {
                holder.itemView.setBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        R.color.divivder_grey
                    )
                )
                holder.tvSprinter.setTypeface(holder.tvSprinter.typeface, Typeface.BOLD)
            } else {
                holder.itemView.setBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        R.color.white
                    )
                )
                holder.tvSprinter.setTypeface(holder.tvSprinter.typeface, Typeface.NORMAL)
            }

            holder.itemView.setOnClickListener {
                selected = position
                listener.onItemSelected(position)
                notifyDataSetChanged()
            }
        }else{
            holder.tvReason.visibility = View.VISIBLE
            holder.tvReason.text = sprinter.restrictionReason?: "Pending Outstanding Amount"
            holder.itemView.setBackgroundColor(
                ContextCompat.getColor(
                    context,
                    R.color.disable_button
                )
            )
        }

    }

    class SprinterHolder(view : View) : RecyclerView.ViewHolder(view){

        val tvSprinter: TextView = view.findViewById(R.id.tvSprinter)
        val tvReason: TextView = view.findViewById(R.id.tvRestrictionReason)

    }
}