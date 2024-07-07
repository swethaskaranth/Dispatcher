package com.goflash.dispatch.features.cash.presenter

import android.content.Context
import android.net.Uri
import com.goflash.dispatch.data.CashPickupDTO
import com.goflash.dispatch.features.cash.view.AddCashPickupView
import java.io.File

interface AddCashPickupPresenter {

    fun onAttachView(context: Context, view: AddCashPickupView)

    fun onDetachView()

    fun getData()

    fun submitCashPickup(cashpickup : CashPickupDTO?)

    fun uploadFile(currentPhotoPath: File?, type: String)

    fun createImageFile(context : Context,albumName: String): File

    fun deleteTempFiles(context : Context,albumName: String)

    fun compressImage(path : String, output: File, uri: Uri)
}