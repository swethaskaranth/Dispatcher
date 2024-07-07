package com.goflash.dispatch.ui.adapter

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.goflash.dispatch.R
import com.goflash.dispatch.app_constants.album_name2
import com.goflash.dispatch.listeners.OnItemSelected
import com.goflash.dispatch.util.getAlbumStorageDir
import java.io.File

class CreditImageAdapter(val context: Context, val list: List<String>, private val feedback: Boolean, private val onItemSelected: OnItemSelected) : RecyclerView.Adapter<CreditImageAdapter.CreditViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): CreditViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.image_items, parent, false)
        return CreditViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: CreditViewHolder, position: Int){

        val lot = list[position]
        val mImageBitmap: Bitmap?
        mImageBitmap = if(feedback)
            MediaStore.Images.Media.getBitmap(context.contentResolver, Uri.fromFile(File(lot)))
        else
            MediaStore.Images.Media.getBitmap(context.contentResolver, Uri.fromFile(File(getAlbumStorageDir(context, album_name2),lot)))

        holder.ivImage.setImageBitmap(mImageBitmap)

        holder.ivRemove.setOnClickListener {
            onItemSelected.onItemSelected(position)
        }

    }

    class CreditViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

        val ivImage: ImageView = view.findViewById(R.id.iv_show_image)
        val ivRemove: ImageView = view.findViewById(R.id.iv_remove)

    }

}