package com.goflash.dispatch.features.lastmile.settlement.presenter

import android.content.Context
import android.net.Uri
import com.goflash.dispatch.features.lastmile.settlement.view.ReviewImagesView
import com.goflash.dispatch.type.AckStatus
import java.io.File

interface ReviewImagesPresenter {

    fun onAttach(context: Context, view : ReviewImagesView)

    fun onDetach()

    fun getAckSlipsForLBN(tripId: Long, lbn: String)

    fun onItemSelected(position: Int, status: AckStatus)

    fun approveImages()

    fun createImageFile(context : Context,albumName: String): File

    fun deleteTempFiles(context : Context,albumName: String)

    fun compressImage(path : String, output: File, uri: Uri)

    fun uploadFile(currentPhotoPath: File?, lbn : String)

    fun onItemClicked(position: Int)
}