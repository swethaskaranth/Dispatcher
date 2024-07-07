package com.goflash.dispatch.features.lastmile.tripCreation.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.goflash.dispatch.R
import com.goflash.dispatch.data.UnassignedDTO
import com.goflash.dispatch.features.lastmile.tripCreation.listeners.OnShipmentSelectedListener
import com.goflash.dispatch.type.PriorityType

class AddShipmentAdapter(
    private val context: Context,
    private val list: Map<String, List<UnassignedDTO>>,
    private val listener: OnShipmentSelectedListener,
    private val keys: List<String> = ArrayList(list.keys)
) :
    RecyclerView.Adapter<AddShipmentAdapter.AddShipmentHolder>() {

    private var bind = false
    private val viewPool = RecyclerView.RecycledViewPool()
    private var mAdapter: MutableMap<String, NestedShipmentAdapter?> = mutableMapOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddShipmentHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.layout_add_shipment_item, parent, false)
        return AddShipmentHolder(view)
    }

    override fun onBindViewHolder(holder: AddShipmentHolder, position: Int) {

        bind = true

        holder.tvPincode.text = keys[position]
        holder.tvTaskCount.text = "${list[keys[position]]?.size}"

        val childLayoutManager =
            LinearLayoutManager(holder.recyclerView.context, RecyclerView.VERTICAL, false)
        childLayoutManager.initialPrefetchItemCount = 2000

        if (!mAdapter.containsKey(keys[position]))
            mAdapter[keys[position]] = NestedShipmentAdapter(
                context,
                list[keys[position]]?.sortedByDescending { it.priorityType == PriorityType.HIGH.name }!!,
                listener
            )
        //mAdapter.add(position,NestedShipmentAdapter(context, list[keys[position]]?.sortedByDescending { it.priorityType == PriorityType.HIGH.name }!!,listener))
        holder.recyclerView.apply {
            layoutManager = childLayoutManager
            adapter = mAdapter[keys[position]]
            setRecycledViewPool(viewPool)
        }

        holder.ivExpand.setOnClickListener {
            if (holder.recyclerView.visibility == View.VISIBLE) {
                holder.ivExpand.rotation = holder.ivExpand.rotation + 180
                holder.recyclerView.visibility = View.GONE
                holder.clHeader.setBackgroundResource(R.drawable.header_background)
            } else {
                holder.ivExpand.rotation = holder.ivExpand.rotation + 180
                holder.recyclerView.visibility = View.VISIBLE
                holder.clHeader.setBackgroundResource(R.drawable.header_background_expanded)
            }
        }

        holder.cbSelectAll.isChecked = list[keys[position]]?.any { it.selected == false } != true

        holder.ivHighPriority.visibility =
            if (list[keys[position]]?.any { it.priorityType == PriorityType.HIGH.name } == true) View.VISIBLE else View.GONE


        holder.cbSelectAll.setOnCheckedChangeListener { v, isChecked ->
            if (!bind) {
                if (isChecked) {
                    listener.onShipmentSelected(list[keys[position]]!!)
                    for (shipment in list[keys[position]]!!)
                        shipment.selected = true
                } else {
                    listener.onShipmentUnselected(list[keys[position]]!!)
                    for (shipment in list[keys[position]]!!)
                        shipment.selected = false
                }

                holder.recyclerView.adapter?.notifyDataSetChanged()
            }

        }

        bind = false
    }

    override fun getItemCount(): Int {
        return keys.size
    }

    fun updateAdapter(pincode: String, position: Int) {
        mAdapter[pincode]?.notifyItemChanged(position)
    }

    class AddShipmentHolder(view: View) : RecyclerView.ViewHolder(view) {

        val cbSelectAll: CheckBox = view.findViewById(R.id.cbSelectAll)
        val ivHighPriority: ImageView = view.findViewById(R.id.ivHighPriority)
        val recyclerView: RecyclerView = view.findViewById(R.id.rvShipments)
        val ivExpand: ImageView = view.findViewById(R.id.ivExpand)
        val tvPincode: TextView = view.findViewById(R.id.tvPincode)
        val tvTaskCount: TextView = view.findViewById(R.id.tvTaskCount)
        val clHeader: ConstraintLayout = view.findViewById(R.id.clHeader)

    }
}