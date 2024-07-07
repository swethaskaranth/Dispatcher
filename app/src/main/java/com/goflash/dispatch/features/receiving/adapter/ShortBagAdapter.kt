package com.goflash.dispatch.features.receiving.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import com.goflash.dispatch.R
import com.goflash.dispatch.data.VehicleDetails
import com.goflash.dispatch.listeners.OnSpinnerItemSelected

class ShortBagAdapter(private val context : Context, val items: MutableList<VehicleDetails>, private val OnSpinnerItemSelected : OnSpinnerItemSelected) : androidx.recyclerview.widget.RecyclerView.Adapter<ShortBagAdapter.ReceivingHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReceivingHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_short,parent,false)
        return ReceivingHolder(view)
    }

    override fun onBindViewHolder(holder: ReceivingHolder, position: Int) {
        val lot = items[position]

        holder.txtBagId.text = lot.bagId

        val reason = mutableListOf<String>()
        reason.add(context.getString(R.string.select_reason))
        reason.add(context.getString(R.string.damaged_barcode))
        reason.add(context.getString(R.string.bag_not_received))

        val adapter = ArrayAdapter(context, R.layout.spinner_layout, reason)
        holder.spinner.adapter = adapter

        try {
            val popup = Spinner::class.java.getDeclaredField("mPopup")
            popup.isAccessible = true

            // Get private mPopup member variable and try cast to ListPopupWindow
            val popupWindow = popup.get(holder.spinner) as android.widget.ListPopupWindow

            // Set popupWindow height to 500px
            popupWindow.height = context.resources.getDimension(R.dimen.margin_100).toInt()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        holder.spinner.setSelection(adapter.getPosition(lot.returnReason))

        holder.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {

                OnSpinnerItemSelected.onItemSelected(position, pos, reason, lot)
            }
        }

    }

    override fun getItemCount(): Int {
        return items.size
    }

    class ReceivingHolder(v : View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(v){
        val view: View = v
        val txtBagId: TextView = view.findViewById(R.id.txt_bagId)
        val spinner: Spinner = view.findViewById(R.id.spinner)
    }
}