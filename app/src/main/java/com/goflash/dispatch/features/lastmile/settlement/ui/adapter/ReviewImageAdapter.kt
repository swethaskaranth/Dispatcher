package com.goflash.dispatch.features.lastmile.settlement.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.goflash.dispatch.R
import com.goflash.dispatch.data.AckSlipDto
import com.goflash.dispatch.listeners.ImageClickListener
import com.goflash.dispatch.type.AckStatus

class ReviewImageAdapter(private val context: Context, private val list: MutableList<AckSlipDto>, private val listener: ImageClickListener): RecyclerView.Adapter<ReviewImageAdapter.ReviewImageHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewImageHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_review_image_item, parent, false)
        return ReviewImageHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewImageHolder, position: Int) {
        val item = list[position]
        Glide.with(context)
            .load(item.url)
            .placeholder(R.drawable.ic_add_image)
            .into(holder.image)

        when(item.status){
            AckStatus.ACCEPTED.name -> {
                holder.status.visibility = View.VISIBLE
                holder.status.text = context.getString(R.string.approved)
                holder.status.setBackgroundResource(R.drawable.approved_ribbon)
                holder.accept.setBackgroundColor(ContextCompat.getColor(context, R.color.approve_image_with_opacity))
                holder.reject.setBackgroundColor(ContextCompat.getColor(context, R.color.reject_background))
            }
            AckStatus.REJECTED.name -> {
                holder.status.visibility = View.VISIBLE
                holder.status.text = context.getString(R.string.rejected)
                holder.status.setBackgroundResource(R.drawable.rejected_ribbon)
                holder.reject.setBackgroundColor(ContextCompat.getColor(context, R.color.reject_background_with_opacity))
                holder.accept.setBackgroundColor(ContextCompat.getColor(context, R.color.approve_image_color))
            }
            else -> {
                holder.status.visibility = View.GONE
            }
        }

        holder.itemView.setOnClickListener{
            listener.onImageClicked(position)
        }

        holder.reject.setOnClickListener {
            listener.onImageRejected(position)
            holder.reject.setBackgroundColor(ContextCompat.getColor(context, R.color.reject_background_with_opacity))
            holder.accept.setBackgroundColor(ContextCompat.getColor(context, R.color.approve_image_color))
        }

        holder.accept.setOnClickListener {
            listener.onImageApproved(position)
            holder.accept.setBackgroundColor(ContextCompat.getColor(context, R.color.approve_image_with_opacity))
            holder.reject.setBackgroundColor(ContextCompat.getColor(context, R.color.reject_background))
        }

        /*holder.itemView.setOnLongClickListener {
            holder.check.visibility = if(holder.check.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            listener.onImageLongClicked(position)
            true
        }*/
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun addAckSLip(ackSlipDto: AckSlipDto){
       // list.add(ackSlipDto)
        notifyItemInserted(list.size-1)
    }

    class ReviewImageHolder(view: View): RecyclerView.ViewHolder(view){
        val image: ImageView = view.findViewById(R.id.ivAckImage)
       // val check: ImageView = view.ivCheck
        val status: TextView = view.findViewById(R.id.tvStatus)
        val reject: TextView = view.findViewById(R.id.reject)
        val accept : ImageView = view.findViewById(R.id.accept)
    }
}