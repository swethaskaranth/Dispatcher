package com.goflash.dispatch.features.lastmile.settlement.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.goflash.dispatch.R
import com.goflash.dispatch.data.AckSlipDto

class ImageAdapter(private val context: Context, private val list: List<AckSlipDto>) :
    RecyclerView.Adapter<ImageAdapter.ImageHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.image_holder, parent, false)
        return ImageHolder(view)
    }

    override fun onBindViewHolder(holder: ImageHolder, position: Int) {
        val item = list[position]
        Glide.with(context)
            .load(item.url)
            .placeholder(R.drawable.ic_add_image)
            .into(holder.image)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ImageHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.imageView)
    }
}