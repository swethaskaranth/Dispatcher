package com.goflash.dispatch.presenter.impl

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import com.goflash.dispatch.BuildConfig
import com.goflash.dispatch.api_services.SessionService
import com.goflash.dispatch.app_constants.SIGNATURE_MEDIA_TYPE
import com.goflash.dispatch.app_constants.model
import com.goflash.dispatch.di.interactor.SortationApiInteractor
import com.goflash.dispatch.presenter.RaiseTicketPresenter
import com.goflash.dispatch.presenter.views.RaiseTicketView
import com.goflash.dispatch.util.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Ravi on 14/03/19.
 *
 */
class RaiseTicketPresenterImpl(private val interactor: SortationApiInteractor) : RaiseTicketPresenter {

    private val TAG = RaiseTicketPresenterImpl::class.java.simpleName

    private var mView: RaiseTicketView? = null

    private var compositeSubscription: CompositeSubscription? = null

    private var fileList = mutableListOf<MultipartBody.Part>()

    private var context: Context? = null
    lateinit var email: RequestBody

    override fun onAttachView(context: Context, view: RaiseTicketView) {
        this.mView = view
        this.context = context
        compositeSubscription = CompositeSubscription()

    }

   private fun sendFeedbackDetails(file: MutableList<MultipartBody.Part>, title: RequestBody, description: RequestBody, priority: RequestBody, product: RequestBody, email: RequestBody,assetName: RequestBody) {
        compositeSubscription?.add(interactor.uploadFeedbackImage(file, title, description, priority, product, email,assetName)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    mView?.onSuccess()
                }, { error: Throwable? ->
                    mView?.onFailure(error)
                }))
    }

    override fun onDetachView() {
        if (this.mView == null)
            return
        mView = null
        compositeSubscription?.unsubscribe()
        compositeSubscription = null
    }

   override fun uploadFile(currentPhotoPath: MutableList<File>, description: String, title: String, cc: String, priority: String) {
        for(i in 0 until currentPhotoPath.size){
           val fileBody = createMultipartFormData(currentPhotoPath[i])
           fileList.add(fileBody)
        }

       email = if(cc.isEmpty())
           (SessionService.email?:"").toRequestBody(MultipartBody.FORM)
       else
           "${SessionService.email?:""},$cc".toRequestBody(MultipartBody.FORM)

        val header = title.toRequestBody(MultipartBody.FORM)
        val description = getDescription(description).toRequestBody(MultipartBody.FORM)
        val priority = priority.toRequestBody(MultipartBody.FORM)
        val product = "Logistics".toRequestBody(MultipartBody.FORM)
       val assetName = PreferenceHelper.assignedAssetName.toRequestBody(MultipartBody.FORM)

        sendFeedbackDetails(fileList, header, description, priority, product, email,assetName)
   }

    @Throws(IOException::class)
    override fun createImageFile(context : Context,albumName: String): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())

        val storageDir = File(context.filesDir,albumName)

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

    /**
     * create file form data
     * @param signatureFile [ signature file]
     * */
    private fun createMultipartFormData(signatureFile: File): MultipartBody.Part {
        // create RequestBody instance from file
        val file = File(signatureFile.toString())
        val requestFile = file
                .asRequestBody(SIGNATURE_MEDIA_TYPE.toMediaTypeOrNull())
        ////Log.d(TAG, "$file")
        return MultipartBody.Part.createFormData("screenshot", file.name, requestFile)
    }

    private fun getDescription(description: String): String{
        return "$description \nName: ${SessionService.name}\nAsset Name: ${PreferenceHelper.assignedAssetName}\nVersion: ${BuildConfig.VERSION_NAME}\nDevice: $model"
    }

    override suspend fun compressImage(path : String,output: File, uri: Uri){
        val (hgt, wdt) = context!!.getImageHgtWdt(uri)
        val bmp = decodeFile(context!!, uri,hgt, wdt, ScalingLogic.FIT)
        if(bmp != null) {
            val stream = ByteArrayOutputStream()
            bmp.compress(Bitmap.CompressFormat.JPEG, 90, stream)
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

        file.delete()
    }

    private fun decodeFile(context: Context, uri: Uri, dstWidth: Int, dstHeight: Int, scalingLogic: ScalingLogic): Bitmap? {
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

