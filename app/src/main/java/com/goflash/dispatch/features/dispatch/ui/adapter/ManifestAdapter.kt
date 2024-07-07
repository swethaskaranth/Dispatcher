package com.goflash.dispatch.features.dispatch.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.goflash.dispatch.R
import com.goflash.dispatch.data.MidMileDispatchedRunsheet
import com.goflash.dispatch.features.lastmile.tripCreation.listeners.OnItemSelctedListener
import com.goflash.dispatch.util.getTimeFromISODate

class ManifestAdapter(private val context: Context, private val manifestList: List<MidMileDispatchedRunsheet>, private val listener: OnItemSelctedListener): RecyclerView.Adapter<ManifestAdapter.ManifestViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ManifestViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.midmile_runsheet_item, parent, false)
        return ManifestViewHolder(view)
    }

    override fun onBindViewHolder(holder: ManifestViewHolder, position: Int) {
        val manifest = manifestList[position]

        holder.manifestId.text = "Runsheet #${manifest.tripId} - ${manifest.id}"
        holder.dispatchTime.text = getTimeFromISODate(manifest.dispatchTime!!)

        holder.printManifest.setOnClickListener {
            listener.onItemSelected(manifest.runsheetUrl!!, "Runsheet #${manifest.tripId} - ${manifest.id}")
        }

    }

    override fun getItemCount(): Int = manifestList.size


    class ManifestViewHolder(view: View): ViewHolder(view){
        val manifestId: TextView = view.findViewById(R.id.runsheetId)
        val dispatchTime: TextView = view.findViewById(R.id.dispatchTime)
        val printManifest: TextView = view.findViewById(R.id.printManifest)

    }
}