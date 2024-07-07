package com.goflash.dispatch.features.lastmile.tripCreation.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.goflash.dispatch.R
import com.goflash.dispatch.data.SprinterForZone
import com.goflash.dispatch.features.lastmile.tripCreation.listeners.OnItemSelctedListener
import com.goflash.dispatch.features.lastmile.tripCreation.listeners.ZoneItemListener

class AssignSprinterAdapter (private val context: Context,private val zoneId : Int, private val sprinters: List<SprinterForZone>, private val listener : ZoneItemListener) : RecyclerView.Adapter<AssignSprinterAdapter.SprinterHolder>(){

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AssignSprinterAdapter.SprinterHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_sprinter,parent,false)
        return AssignSprinterAdapter.SprinterHolder(view)
    }

    override fun onBindViewHolder(holder: AssignSprinterAdapter.SprinterHolder, position: Int) {

        val sprinter = sprinters[position]

        holder.tvName.text = sprinter.name

        holder.itemView.setOnClickListener{
            listener.onItemSelected(zoneId)
        }

        holder.ivRemove.setOnClickListener{
            listener.removeSprinter(zoneId,sprinter)
        }
    }

    override fun getItemCount(): Int {
        return sprinters.size
    }


    class SprinterHolder(view : View) : RecyclerView.ViewHolder(view){

        val tvName : TextView = view.findViewById(R.id.tvName)
        val ivRemove : ImageView = view.findViewById(R.id.ivRemove)

    }
}