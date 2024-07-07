package com.goflash.dispatch.features.receiving.presenter

import android.content.Context
import com.goflash.dispatch.features.receiving.view.PreviewView
import java.io.File

/**
 *Created by Ravi on 2019-09-08.
 */
interface PreviewPresenter {

    fun onAttachView(context: Context, view: PreviewView)

    fun onDetachView()

    fun createImageFile(context : Context,albumName: String): File

    fun compressImage(path : String,output: File)

    fun deleteTempFiles(context : Context,albumName: String)
}