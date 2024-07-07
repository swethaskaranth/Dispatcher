package com.goflash.dispatch.presenter

import android.content.Context
import android.net.Uri
import com.goflash.dispatch.presenter.views.RaiseTicketView
import java.io.File

/**
 * Created by Ravi on 14/03/19.
 *
 */
interface RaiseTicketPresenter {

    fun onAttachView(context: Context, view: RaiseTicketView)

    fun onDetachView()

    fun uploadFile(currentPhotoPath: MutableList<File>, description: String, title: String, cc: String, priority: String)

    fun createImageFile(context : Context,albumName: String): File

    suspend fun compressImage(path : String, output: File, uri: Uri)

    fun deleteTempFiles(context : Context,albumName: String)
}