package com.goflash.dispatch.features.dispatch.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.goflash.dispatch.R
import com.goflash.dispatch.data.BagDTO
import com.goflash.dispatch.type.BagWeight
import com.goflash.dispatch.type.BagWeight.Companion.getWeightFromPosition

class SizeSelectionAdapter(private val context: Context, private val bags: List<BagDTO>, private val listener: onSpinnerItemSelected): RecyclerView.Adapter<SizeSelectionAdapter.SizeSelectionHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SizeSelectionHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.bag_size_selection_item, parent, false)
        return SizeSelectionHolder(view)
    }

    override fun onBindViewHolder(holder: SizeSelectionHolder, position: Int) {
        val bag = bags[position]
        holder.bagId.text = bag.bagId

        holder.spinner.adapter = ArrayAdapter(context, R.layout.spinner_layout, BagWeight.toStringList())
        if (bag.weight != null && bag.weight != 0.0)
            holder.spinner.setSelection(BagWeight.getPositionFromWeight(bag.weight))

        holder.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                listener.onItemSelected(bag, getWeightFromPosition(pos))
            }
        }

    }

    override fun getItemCount(): Int {
        return bags.size
    }

    class SizeSelectionHolder(view: View): ViewHolder(view){
        val bagId: TextView = view.findViewById(R.id.txtBagId)
        val spinner: Spinner = view.findViewById(R.id.spSize)
    }

    interface onSpinnerItemSelected{

        fun onItemSelected(bag: BagDTO, size: Double)
    }
}