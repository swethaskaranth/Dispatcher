package com.goflash.dispatch.features.lastmile.settlement.ui.adapter

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.goflash.dispatch.R
import com.goflash.dispatch.data.Item
import com.goflash.dispatch.features.lastmile.settlement.listeners.ReviewItemListener
import com.goflash.dispatch.type.ReconStatus
import java.util.regex.Pattern

class ScannedItemAdapter(
    private val context: Context,
    private val items: List<Item>,
    private val listener: ReviewItemListener,
    private val partialDelivery: Boolean
) :
    RecyclerView.Adapter<ScannedItemAdapter.ScannedItemHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScannedItemHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.layout_scanned_adapter_item, parent, false)
        return ScannedItemHolder(view)
    }

    override fun onBindViewHolder(holder: ScannedItemHolder, position: Int) {
        val item = items[position]

        if (item.reconStatus == null) {
            setEditView(holder, item, position)
        } else {
            holder.clScannedItem.visibility = View.GONE
            holder.clReviewedItem.visibility = View.VISIBLE

            holder.tvName.text = item.displayName
            holder.tvBatch.text =
                String.format(context.getString(R.string.batch_number), item.batchNumber)
            holder.tvReason.text =
                String.format(context.getString(R.string.return_reason), item.returnReason)

            if (item.reconStatus == ReconStatus.REJECT.name) {
                holder.itemView.setBackgroundResource(R.drawable.reject_item_background)
                holder.tvRejectionReason.visibility = View.VISIBLE
                holder.tvRejectionReason.text =
                    String.format(
                        context.getString(R.string.rejection_reason),
                        item.reconStatusReason
                    )
            } else {
                holder.itemView.setBackgroundResource(R.drawable.accept_item_background)
                holder.tvRejectionReason.visibility = View.GONE
            }

            holder.ivEdit.setOnClickListener {
                setEditView(holder, item, position)
            }
        }

    }

    private fun setEditView(holder: ScannedItemHolder, item: Item, position: Int) {

        holder.clScannedItem.visibility = View.VISIBLE
        holder.clReviewedItem.visibility = View.GONE
        holder.tvScannedItemName.text = item.displayName
        holder.tvScannedItemBatch.text =
            String.format(context.getString(R.string.batch_number), item.batchNumber)
        holder.tvScannedItemReason.text =
            String.format(context.getString(R.string.return_reason), item.returnReason)

        holder.tvAccept.visibility = View.VISIBLE
        if (partialDelivery) {
            holder.tvReject.visibility = View.GONE
        } else
            holder.tvReject.visibility = View.VISIBLE
        holder.clRejectionReason.visibility = View.GONE

        var returnReason: String? = null

        var reconStatus: ReconStatus? = null

        holder.tvAccept.setOnClickListener {
            reconStatus = ReconStatus.ACCEPT
            listener.onAcceptOrRejectSelected(position, reconStatus!!, "", "")
        }

        holder.tvReject.setOnClickListener {
            reconStatus = ReconStatus.REJECT
            holder.clRejectionReason.visibility = View.VISIBLE
            holder.tvAccept.visibility = View.GONE
            holder.tvReject.visibility = View.GONE

            holder.rgReason.clearCheck()

            holder.rgReason.setOnCheckedChangeListener { group, checkedId ->
                when (checkedId) {
                    R.id.rbRefrigerated -> returnReason = holder.rbRefrigerated.text.toString()
                    R.id.rbWrongBatch -> returnReason = holder.rbWrongBatch.text.toString()
                    R.id.rbWrongMed -> returnReason = holder.rbWrongMed.text.toString()
                    R.id.rbDoesNotBelong -> returnReason =
                        holder.rbDoesNotBelong.text.toString()

                }
            }
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

            }
        })

        holder.tvConfirm.setOnClickListener {
            if (returnReason == null)
                Toast.makeText(context, "Please select reason", Toast.LENGTH_SHORT).show()
            else if (holder.labelInvalid.visibility == View.VISIBLE)
                Toast.makeText(context, "Please correct the remarks", Toast.LENGTH_SHORT).show()
            else
                listener.onAcceptOrRejectSelected(position, reconStatus!!, returnReason!!, holder.etRemarks.text.toString())
        }

        holder.tvCancel.setOnClickListener {
            returnReason = null
            reconStatus = null
            holder.tvAccept.visibility = View.VISIBLE
            holder.tvReject.visibility = View.VISIBLE
            holder.clRejectionReason.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class ScannedItemHolder(view: View) : RecyclerView.ViewHolder(view) {

        val clScannedItem: View = view.findViewById(R.id.scanned_item)
        val clReviewedItem: View = view.findViewById(R.id.reviewed_item)

        val tvScannedItemName: TextView = clScannedItem.findViewById(R.id.tvItemName)
        val tvScannedItemBatch: TextView = clScannedItem.findViewById(R.id.tvItemBatch)
        val tvScannedItemReason: TextView = clScannedItem.findViewById(R.id.tvItemReason)
        val tvAccept: TextView = clScannedItem.findViewById(R.id.tvAccept)
        val tvReject: TextView = clScannedItem.findViewById(R.id.tvReject)

        val clRejectionReason: ConstraintLayout = clScannedItem.findViewById(R.id.rejection_reasom)
        val rgReason: RadioGroup = clScannedItem.findViewById(R.id.rgReason)
        val tvCancel: TextView = clScannedItem.findViewById(R.id.tvCancel)
        val tvConfirm: TextView = clScannedItem.findViewById(R.id.tvConfirm)

        val rbRefrigerated: RadioButton = clScannedItem.findViewById(R.id.rbRefrigerated)
        val rbWrongBatch: RadioButton = clScannedItem.findViewById(R.id.rbWrongBatch)
        val rbWrongMed: RadioButton = clScannedItem.findViewById(R.id.rbWrongMed)
        val rbDoesNotBelong: RadioButton = clScannedItem.findViewById(R.id.rbDoesNotBelong)

        val etRemarks: EditText = clScannedItem.findViewById(R.id.etRemarks)
        val labelInvalid : TextView = clScannedItem.findViewById(R.id.tvInvalid)

        val tvName: TextView = clReviewedItem.findViewById(R.id.tvName)
        val ivEdit: ImageView = clReviewedItem.findViewById(R.id.ivEdit)
        val tvBatch: TextView = clReviewedItem.findViewById(R.id.tvBatch)
        val tvReason: TextView = clReviewedItem.findViewById(R.id.tvReason)
        val tvRejectionReason: TextView = clReviewedItem.findViewById(R.id.tvRejectionReason)

    }
}