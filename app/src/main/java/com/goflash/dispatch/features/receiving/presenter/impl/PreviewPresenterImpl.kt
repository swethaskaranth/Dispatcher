package com.goflash.dispatch.features.receiving.presenter.impl

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import com.goflash.dispatch.di.interactor.SortationApiInteractor
import com.goflash.dispatch.features.receiving.presenter.PreviewPresenter
import com.goflash.dispatch.features.receiving.view.PreviewView
import com.goflash.dispatch.util.Util.decodeImageFromFiles
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 *Created by Ravi on 2019-09-08.
 */
class PreviewPresenterImpl(val sortationApiInteractor: SortationApiInteractor): PreviewPresenter {

    private var context: Context? = null
    private var previewView: PreviewView? = null

    override fun onAttachView(context: Context, view: PreviewView) {
        this.context = context
        this.previewView = view
    }

    override fun onDetachView() {

    }

    @SuppressLint("SimpleDateFormat")
    @Throws(IOException::class)
    override fun createImageFile(context : Context,albumName: String): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())

        val storageDir = File(context.filesDir,albumName)

        if (!storageDir.mkdirs())
            storageDir.mkdir()
        //val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val f = File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        )

        return f
    }


    override fun compressImage(path : String,output: File){
        val bmp = decodeImageFromFiles(path,300,300)
        if(bmp != null){
            val stream = ByteArrayOutputStream()
            bmp.compress(Bitmap.CompressFormat.JPEG,90,stream)
            val fos = FileOutputStream(output)
            fos.write(stream.toByteArray())
            fos.flush()
            fos.close()
            stream.close()
        }
    }

    override fun deleteTempFiles(context : Context,albumName: String){
        val file = File(context.filesDir,albumName)
        if (file.exists()) {
            val entries = file.list()
            for (s in entries) {
                val currentFile = File(file.path, s)
                currentFile.delete()
            }
        }
        file.delete()}
}