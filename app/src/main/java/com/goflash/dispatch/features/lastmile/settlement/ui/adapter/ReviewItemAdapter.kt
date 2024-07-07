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


class ReviewItemAdapter(
    private val context: Context,
    private val list: List<Item>,
    private val listener: ReviewItemListener,
    private val partialDelivery: Boolean
) :
    RecyclerView.Adapter<ReviewItemAdapter.ReviewItemHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewItemHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_review_item, parent, false)
        return ReviewItemHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewItemHolder, position: Int) {
        val item = list[position]

        if (item.reconStatus == null) {
            setEditView(holder, position)
        } else {
            holder.clReviewed.visibility = View.VISIBLE
            holder.clAcceptReject.visibility = View.GONE

            holder.reviewedBarcode.text = item.barcode
            holder.reason.text = item.reconStatusReason

            if (item.reconStatus == ReconStatus.ACCEPT.name)
                holder.itemView.setBackgroundResource(R.drawable.accept_item_background)
            else
                holder.itemView.setBackgroundResource(R.drawable.reject_item_background)

            holder.edit.setOnClickListener { setEditView(holder, position) }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    private fun setEditView(holder: ReviewItemHolder, position: Int) {

        val item = list[position]

        holder.clAcceptReject.visibility = View.VISIBLE
        holder.clReviewed.visibility = View.GONE

        holder.barcode.text = item.barcode

        var returnReason: String? = null

        var reconStatus: ReconStatus? = null

        if (partialDelivery) {
            holder.reject.visibility = View.GONE
            holder.divider.visibility = View.GONE
        }

        holder.accept.setOnClickListener {
            reconStatus = ReconStatus.ACCEPT

            holder.clReason.visibility = View.VISIBLE
            holder.labelSelect.text = context.getString(R.string.select_accept_reason)
            holder.rgAcceptReason.visibility = View.VISIBLE
            holder.rgRejectReason.visibility = View.GONE

            holder.accept.visibility = View.GONE
            holder.reject.visibility = View.GONE
            holder.divider.visibility = View.GONE

            holder.rgAcceptReason.clearCheck()

            holder.rgAcceptReason.setOnCheckedChangeListener { group, checkedId ->
                when (checkedId) {
                    R.id.rbNoBarcode -> returnReason = holder.rbNoBarcode.text.toString()
                    R.id.rbDamaged -> returnReason = holder.rbDamaged.text.toString()
                    R.id.rbLost -> returnReason = holder.rbLost.text.toString()
                    R.id.rbDidntReceive -> returnReason = holder.rbDidntReceive.text.toString()

                }
            }
        }

        holder.reject.setOnClickListener {
            reconStatus = ReconStatus.REJECT

            holder.clReason.visibility = View.VISIBLE
            holder.labelSelect.text = context.getString(R.string.select_rejection_reason)
            holder.rgRejectReason.visibility = View.VISIBLE
            holder.rgAcceptReason.visibility = View.GONE

            holder.accept.visibility = View.GONE
            holder.reject.visibility = View.GONE
            holder.divider.visibility = View.GONE

            holder.rgRejectReason.clearCheck()

            holder.rgRejectReason.setOnCheckedChangeListener { group, checkedId ->
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

        holder.confirm.setOnClickListener {
            if (returnReason == null)
                Toast.makeText(context, "Please select reason", Toast.LENGTH_SHORT).show()
            else if (holder.labelInvalid.visibility == View.VISIBLE)
                Toast.makeText(context, "Please correct the remarks", Toast.LENGTH_SHORT).show()
            else
                listener.onAcceptOrRejectSelected(position, reconStatus!!, returnReason!!, holder.etRemarks.text.toString())
        }

        holder.cancel.setOnClickListener {
            returnReason = null
            reconStatus = null
            holder.clReason.visibility = View.GONE

            holder.accept.visibility = View.VISIBLE
            if (partialDelivery)
                holder.reject.visibility = View.GONE
            else
                holder.reject.visibility = View.VISIBLE
            holder.divider.visibility = View.VISIBLE

        }

    }

    class ReviewItemHolder(view: View) : RecyclerView.ViewHolder(view) {

        val clAcceptReject: View = view.findViewById(R.id.clAcceptReject)
        val clReviewed: View = view.findViewById(R.id.clReviewed)

        val barcode: TextView = clAcceptReject.findViewById(R.id.tvBarcode)
        val accept: TextView = clAcceptReject.findViewById(R.id.tvAccept)
        val reject: TextView = clAcceptReject.findViewById(R.id.tvReject)
        val divider: View = clAcceptReject.findViewById(R.id.divider)
        val clReason: ConstraintLayout = clAcceptReject.findViewById(R.id.clReason)
        val labelSelect: TextView = clAcceptReject.findViewById(R.id.labelSelect)
        val rgRejectReason: RadioGroup = clAcceptReject.findViewById(R.id.rgRejectReason)
        val rgAcceptReason: RadioGroup = clAcceptReject.findViewById(R.id.rgAcceptReason)
        val confirm: TextView = clAcceptReject.findViewById(R.id.tvConfirm)
        val cancel: TextView = clAcceptReject.findViewById(R.id.tvCancel)
        val etRemarks: EditText = clAcceptReject.findViewById(R.id.etRemarks)
        val labelInvalid : TextView = clAcceptReject.findViewById(R.id.tvInvalid)

        val rbRefrigerated: RadioButton = clAcceptReject.findViewById(R.id.rbRefrigerated)
        val rbWrongBatch: RadioButton = clAcceptReject.findViewById(R.id.rbWrongBatch)
        val rbWrongMed: RadioButton = clAcceptReject.findViewById(R.id.rbWrongMed)
        val rbDoesNotBelong: RadioButton = clAcceptReject.findViewById(R.id.rbDoesNotBelong)

        val rbNoBarcode: RadioButton = clAcceptReject.findViewById(R.id.rbNoBarcode)
        val rbDamaged: RadioButton = clAcceptReject.findViewById(R.id.rbDamaged)
        val rbLost: RadioButton = clAcceptReject.findViewById(R.id.rbLost)
        val rbDidntReceive: RadioButton = clAcceptReject.findViewById(R.id.rbDidntReceive)

        val reviewedBarcode: TextView = clReviewed.findViewById(R.id.tvName)
        val reason: TextView = clReviewed.findViewById(R.id.tvReason)
        val edit: ImageView = clReviewed.findViewById(R.id.ivEdit)


    }
}