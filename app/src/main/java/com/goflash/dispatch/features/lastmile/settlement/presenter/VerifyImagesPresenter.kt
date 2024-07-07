package com.goflash.dispatch.features.lastmile.settlement.presenter

import android.content.Context
import android.net.Uri
import com.goflash.dispatch.features.lastmile.settlement.view.VerifyImagesView
import java.io.File

interface VerifyImagesPresenter {

    fun onAttachView(context: Context, view : VerifyImagesView)

    fun onDetachView()

    fun setTripId(tripId: Long)

    fun createImageFile(context : Context,albumName: String): File

    fun deleteTempFiles(context : Context,albumName: String)

    fun compressImage(path : String, output: File, uri: Uri)

    fun uploadFile(currentPhotoPath: File?, lbn : String)

    fun getData()

    fun onOrderSelected(lbn: String)

    fun onNext(tripId: Long)
}