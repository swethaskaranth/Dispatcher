package com.goflash.dispatch.features.lastmile.settlement.ui.adapter

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.goflash.dispatch.R
import com.goflash.dispatch.features.lastmile.settlement.listeners.ReconImageSelectListener
import com.goflash.dispatch.util.getAlbumStorageDir
import java.io.File

class ReconImageAdapter(val context: Context, val list: HashMap<String, String>,
                        val listener: ReconImageSelectListener,
                        var keys: List<String> = list.keys.toMutableList()): RecyclerView.Adapter<ViewHolder>() {

    companion object {
        const val TYPE_UPLOAD = 0
        const val TYPE_ITEM: Int = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return if (viewType == TYPE_ITEM)
            ReconImageHolder(
                LayoutInflater.from(context).inflate(R.layout.item_recon_image, parent, false)
            )
        else
            ReconImageUpload(
                LayoutInflater.from(context)
                    .inflate(R.layout.item_recon_image_upload, parent, false)
            )
    }

    override fun getItemCount(): Int {
        return list.size.plus(1)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if(position == list.size || list.isEmpty()){
            (holder as ReconImageUpload).itemView.setOnClickListener {
                listener.uploadImage()
            }
        }else{
            val item = list[keys[position]]!!

            val mImageBitmap: Bitmap? =
                MediaStore.Images.Media.getBitmap(context.contentResolver, Uri.fromFile(File(item)))

            (holder as ReconImageHolder).ivImage.setImageBitmap(mImageBitmap)

            holder.ivRemove.setOnClickListener {
                listener.removeImage(keys[position])
            }
        }
    }

    fun setKeys(){
        this.keys = this.list.keys.toMutableList()
    }

    override fun getItemViewType(position: Int): Int {
        return if(position == list.size || list.isEmpty())
            TYPE_UPLOAD
        else
            TYPE_ITEM
    }

    class ReconImageHolder(view: View): ViewHolder(view){
        val ivImage = view.findViewById<ImageView>(R.id.ivImage)
        val ivRemove = view.findViewById<ImageView>(R.id.ivRemove)
    }

    class ReconImageUpload(view: View): ViewHolder(view){
        val ivUpload = view.findViewById<ImageView>(R.id.ivUpload)
    }
}