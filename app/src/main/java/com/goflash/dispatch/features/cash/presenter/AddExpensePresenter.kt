package com.goflash.dispatch.features.cash.presenter

import android.content.Context
import android.net.Uri
import com.goflash.dispatch.features.cash.view.AddExpenseView
import java.io.File

interface AddExpensePresenter {

    fun onAttachView(context: Context, view: AddExpenseView)

    fun onDetachView()

    fun getExpenses()

    fun saveExpense() : Boolean

    fun addNewExpense()

    fun submitExpenses()

    fun uploadFile(currentPhotoPath: File?, position : Int, type: String)

    fun getTotalExpenses()

    fun deleteExpense(position : Int)

    fun deleteVoucherImage(position: Int)

    fun createImageFile(context : Context,albumName: String): File

    fun deleteTempFiles(context : Context,albumName: String)

    fun compressImage(path : String, output: File, uri: Uri)


}