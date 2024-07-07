package com.goflash.dispatch.features.lastmile.settlement.ui.adapter

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.goflash.dispatch.R
import com.goflash.dispatch.data.Item
import com.goflash.dispatch.features.lastmile.settlement.listeners.ReceiveItemListener
import com.goflash.dispatch.util.setEditTextCharacterLimit
import java.util.regex.Pattern

class ReceiveItemAdapter(private val context: Context, private val items: List<Item>, private val listener : ReceiveItemListener, private val partialDelivery : Boolean) :
    RecyclerView.Adapter<ReceiveItemAdapter.ReceiveItemHolder>() {

    private var bind = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReceiveItemHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.layout_nonbarcoded_received_item, parent, false)
        return ReceiveItemHolder(view)
    }

    override fun onBindViewHolder(holder: ReceiveItemHolder, position: Int) {
        bind = true
        val item = items[position]

        holder.tvName.text = item.displayName
        holder.tvBatch.text =
            String.format(context.getString(R.string.batch_number), item.batchNumber)

        holder.tvReturnQuanityRaised.text = String.format(
            context.getString(R.string.return_request_quantity),
            item.returnRaisedQuantity
        )

        holder.tvPickedQuantity.text =
            String.format(context.getString(R.string.picked_quantity), item.returnedQuantity)

        var acceptReason : String? = null
        var rejectReason : String? = null

        if(partialDelivery){
            holder.spinnerRejectQuantity.visibility = View.GONE
            holder.spinnerRejectReason.visibility = View.GONE
            holder.labelRejected.visibility = View.GONE
        }

        holder.spinnerAcceptReason.adapter = ArrayAdapter(context,R.layout.layout_reason_spinner_item,R.id.textReason,context.resources.getStringArray(R.array.accept_reason))
        holder.spinnerAcceptReason.setSelection(when(item.reconStatusReason){
            "Delivered without barcode" -> 1
            "Damaged Medicine" -> 2
            "Lost/Damaged by DB" -> 3
            "Customer did not receive the medicine" -> 4
            else -> 0
        })
        holder.spinnerAcceptReason.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if(!bind) {
                    acceptReason =
                        if (position != 0) holder.spinnerAcceptReason.selectedItem.toString() else null
                    if (acceptReason != null)
                        listener.onAcceptorRejectMedicine(
                            item.itemId,
                            item.ucode,
                            item.displayName,
                            item.batchNumber,
                            item.reconAcceptedQuantity,
                            acceptReason!!,
                            true,
                            ""
                        )
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        val acceptQuantityList = mutableListOf<String>()
        for(i in 0..item.quantity)
            acceptQuantityList.add(i.toString())

        holder.spinnerAcceptQuantity.adapter = ArrayAdapter(context,R.layout.layout_reason_spinner_item,R.id.textReason,acceptQuantityList)
        holder.spinnerAcceptQuantity.setSelection(if(item.reconStatusReason == null) 0 else item.reconAcceptedQuantity)
        holder.spinnerAcceptQuantity.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if(!bind) {
                    item.reconAcceptedQuantity = position
                    if (acceptReason != null)
                        listener.onAcceptorRejectMedicine(
                            item.itemId,
                            item.ucode,
                            item.displayName,
                            item.batchNumber,
                            item.reconAcceptedQuantity,
                            acceptReason!!,
                            true,
                            ""
                        )
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }


        holder.spinnerRejectReason.adapter = ArrayAdapter(context,R.layout.layout_reason_spinner_item,R.id.textReason,context.resources.getStringArray(R.array.reject_reason))
        holder.spinnerRejectReason.setSelection(when(item.reconStatusReason){
            "Wrong batch" -> 1
            "Wrong medicine" -> 2
            "Refrigerated medicine" -> 3
            "Medicine does not belong to this order" -> 4
            else -> 0
        })
        if(holder.spinnerRejectQuantity.selectedItemPosition != -1)
            holder.etRemarks.visibility = View.VISIBLE
        else {
            holder.etRemarks.visibility = View.GONE
            holder.labelInvalid.visibility = View.GONE
        }

        holder.etRemarks.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(str: Editable?) {
                item.reconRemark = str?.toString()
                val pattern = Pattern.compile("^([A-Za-z][A-Za-z0-9-_ ]*)$")
                if(str?.isNotEmpty() == true && str.toString()?.let { pattern.matcher(it).find() } == true)
                    holder.labelInvalid.visibility = View.GONE
                else
                    holder.labelInvalid.visibility = View.VISIBLE

                if (rejectReason != null)
                    listener.onAcceptorRejectMedicine(
                        item.itemId,
                        item.ucode,
                        item.displayName,
                        item.batchNumber,
                        item.reconRejectedQuantity,
                        rejectReason!!,
                        false,
                        item.reconRemark?:""
                    )
            }
        })

        holder.spinnerRejectReason.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if(!bind) {
                    holder.etRemarks.visibility = if(position != 0) View.VISIBLE else View.GONE
                    rejectReason =
                        if (position != 0) holder.spinnerRejectReason.selectedItem.toString() else null
                    if (rejectReason != null)
                        listener.onAcceptorRejectMedicine(
                            item.itemId,
                            item.ucode,
                            item.displayName,
                            item.batchNumber,
                            item.reconRejectedQuantity,
                            rejectReason!!,
                            false,
                            item.reconRemark?:""
                        )

                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }


        holder.spinnerRejectQuantity.adapter = ArrayAdapter(context,R.layout.layout_reason_spinner_item,R.id.textReason,acceptQuantityList)
        holder.spinnerRejectQuantity.setSelection(if(item.reconStatusReason == null) 0 else item.reconRejectedQuantity)

        holder.spinnerRejectQuantity.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if(!bind) {
                    holder.etRemarks.visibility = if(position != 0) View.VISIBLE else View.GONE
                    item.reconRejectedQuantity = position
                    if (rejectReason != null)
                        listener.onAcceptorRejectMedicine(
                            item.itemId,
                            item.ucode,
                            item.displayName,
                            item.batchNumber,
                            item.reconRejectedQuantity,
                            rejectReason!!,
                            false,
                            holder.etRemarks.text.toString()
                        )
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        bind = false


    }

    override fun getItemCount(): Int {
        return items.size
    }

    class ReceiveItemHolder(view: View) : RecyclerView.ViewHolder(view) {

        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvBatch: TextView = view.findViewById(R.id.tvBatch)
        val tvReturnQuanityRaised: TextView = view.findViewById(R.id.tvReturnQuanityRaised)
        val tvPickedQuantity: TextView = view.findViewById(R.id.tvPickedQuantity)
        val spinnerAcceptReason : Spinner = view.findViewById(R.id.spAcceptReason)
        val spinnerAcceptQuantity : Spinner = view.findViewById(R.id.spAcceptQuantity)
        val spinnerRejectReason : Spinner = view.findViewById(R.id.spRejectReason)
        val spinnerRejectQuantity : Spinner = view.findViewById(R.id.spRejectQuantity)
        val labelRejected : TextView = view.findViewById(R.id.labelRejected)
        val labelInvalid : TextView = view.findViewById(R.id.tvInvalid)
        val etRemarks: EditText = view.findViewById(R.id.etRemarks)

    }
}