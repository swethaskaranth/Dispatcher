package com.goflash.dispatch.features.cash.presenter.impl

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import co.uk.rushorm.core.RushCore
import co.uk.rushorm.core.RushSearch
import com.goflash.dispatch.app_constants.PDF_MEDIA_TYPE
import com.goflash.dispatch.app_constants.SIGNATURE_MEDIA_TYPE
import com.goflash.dispatch.data.ExpenseDTO
import com.goflash.dispatch.di.interactor.SortationApiInteractor
import com.goflash.dispatch.features.cash.presenter.AddExpensePresenter
import com.goflash.dispatch.features.cash.view.AddExpenseView
import com.goflash.dispatch.util.ScalingLogic
import com.goflash.dispatch.util.calculateSampleSize
import com.goflash.dispatch.util.getBitmapFromUri
import com.goflash.dispatch.util.getImageHgtWdt
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.exp
import kotlin.math.sign

class AddExpensePresenterImpl(private val sortationApiInteractor: SortationApiInteractor) :
    AddExpensePresenter {

    private var mView: AddExpenseView? = null

    private var compositeSubscription: CompositeSubscription? = null

    private val expenseList = mutableListOf<ExpenseDTO>()

    private var context: Context? = null

    override fun onAttachView(context: Context, view: AddExpenseView) {
        this.context = context
        this.mView = view
        compositeSubscription = CompositeSubscription()
    }

    override fun onDetachView() {
        if (mView == null)
            return
        mView = null
        compositeSubscription?.unsubscribe()
        compositeSubscription = null
    }

    override fun getExpenses() {
        expenseList.clear()
        val expenses = RushSearch().find(ExpenseDTO::class.java)
        expenseList.addAll(expenses)
        expenseList.add(ExpenseDTO(null, null, null, 0, false, null))

        mView?.onExpenseFetched(expenseList)
        getTotalExpenses()

    }

    override fun getTotalExpenses() {
        val totalexpense = expenseList.map { it.amount }.reduce { acc, l -> acc.plus(l) } ?: 0
        mView?.setTotalExpense(expenseList.size - 1, totalexpense, totalexpense > 0 )
    }

    override fun saveExpense(): Boolean {
        for (expense in expenseList) {
            if (validateExpense(expense)) {
                expense.isInvalid = true
                mView?.onExpenseFetched(expenseList)
                getTotalExpenses()
                return false
            }
            expense.isInvalid = false
            expense.isAdded = true
            expense.save()
        }
        return true

    }

    override fun addNewExpense() {
        if (saveExpense()) {
            expenseList.add(ExpenseDTO(null, null, null, 0, false, null))

            mView?.onExpenseFetched(expenseList)
            getTotalExpenses()
        }
    }

    override fun submitExpenses() {
        if (saveExpense()) {
            mView?.finishActivity()
        }
    }

    private fun validateExpense(expense: ExpenseDTO): Boolean {
        return expense.expenseType == null || expense.voucherFileUrl == null || expense.voucherNumber == null || expense.amount == null ||  expense.amount <= 0L
    }

    /**
     * create file form data
     * @param signatureFile [ signature file]
     * */
    private fun createMultipartFormData(signatureFile: File, type: String): MultipartBody.Part {
        // create RequestBody instance from file
        val file = File(signatureFile.toString())
        val requestFile = file
            .asRequestBody(if(type == "IMAGE") SIGNATURE_MEDIA_TYPE.toMediaTypeOrNull() else PDF_MEDIA_TYPE.toMediaTypeOrNull())
        ////Log.d(TAG, "$file")
        return MultipartBody.Part.createFormData("file", file.name, requestFile)
    }

    override fun uploadFile(currentPhotoPath: File?, position: Int, type: String) {


        val fileBody =
            createMultipartFormData(currentPhotoPath!!, type)

        mView?.onShowProgress()


        compositeSubscription?.add(
            sortationApiInteractor.uploadCashImage(fileBody)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    expenseList[position].voucherFileUrl = it.url
                    expenseList[position].fileType = type
                    mView?.onExpenseFetched(expenseList)
                    getTotalExpenses()
                }, { error ->
                    mView?.onFailure(error)
                })
        )
    }

    override fun deleteExpense(position: Int) {
        val expense = expenseList[position]
        RushCore.getInstance().delete(expense)
        getExpenses()
    }

    override fun deleteVoucherImage(position: Int) {
        val expense = expenseList[position]
        expense.voucherFileUrl = null

        mView?.onExpenseFetched(expenseList)
    }

    override fun deleteTempFiles(context: Context, albumName: String) {
        val file = File(context.filesDir, albumName)
        if (file.exists()) {
            val entries = file.list()
            for (s in entries) {
                val currentFile = File(file.path, s)
                currentFile.delete()
            }
        }

        file.delete()
    }

    @Throws(IOException::class)
    override fun createImageFile(context: Context, albumName: String): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())

        val storageDir =
            File(context.filesDir, albumName)

        /*val folder = File("${context.getExternalFilesDir(Environment.DIRECTORY_DCIM)}")
        folder.mkdir()*/

        if (!storageDir.mkdirs())
            storageDir.mkdir()
        //val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val f = File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        )
        /*val file = File(folder, "JPEG__$timeStamp.jpg")
        file.createNewFile()*/

        return f
    }

    override fun compressImage(path: String, output: File, uri: Uri) {
        val (hgt, wdt) = context!!.getImageHgtWdt(uri)
        val bmp = decodeFile(context!!, uri, hgt, wdt, ScalingLogic.FIT)
        if (bmp != null) {
            val stream = ByteArrayOutputStream()
            bmp.compress(Bitmap.CompressFormat.JPEG, 90, stream)
            val fos = FileOutputStream(output)
            fos.write(stream.toByteArray())
            fos.flush()
            fos.close()
            stream.close()
        }
    }

    private fun decodeFile(
        context: Context,
        uri: Uri,
        dstWidth: Int,
        dstHeight: Int,
        scalingLogic: ScalingLogic
    ): Bitmap? {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        context.getBitmapFromUri(uri, options)
        options.inJustDecodeBounds = false

        options.inSampleSize = calculateSampleSize(
            options.outWidth,
            options.outHeight,
            dstWidth,
            dstHeight,
            scalingLogic
        )

        return context.getBitmapFromUri(uri, options)
    }

}