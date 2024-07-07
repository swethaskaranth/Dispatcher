package com.goflash.dispatch.features.lastmile.settlement.presenter.impl

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import co.uk.rushorm.core.RushSearch
import com.goflash.dispatch.app_constants.PDF_MEDIA_TYPE
import com.goflash.dispatch.app_constants.SIGNATURE_MEDIA_TYPE
import com.goflash.dispatch.data.AckForRecon
import com.goflash.dispatch.data.AckSlipDto
import com.goflash.dispatch.di.interactor.SortationApiInteractor
import com.goflash.dispatch.features.lastmile.settlement.presenter.ReviewImagesPresenter
import com.goflash.dispatch.features.lastmile.settlement.view.ReviewImagesView
import com.goflash.dispatch.type.AckSource
import com.goflash.dispatch.type.AckStatus
import com.goflash.dispatch.util.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ReviewImagesPresenterImpl(val sortationApiInteractor: SortationApiInteractor) : ReviewImagesPresenter {

    private var mView: ReviewImagesView? = null

    private var ackForRecon: AckForRecon? = null

    private var ackSlips: MutableList<AckSlipDto> = mutableListOf()

    private var selectedList: MutableList<AckSlipDto> = mutableListOf()

    private var context: Context? = null

    private var compositeSubscription: CompositeSubscription? = null

    private var tripId: Long? = null

    override fun onAttach(context: Context, view: ReviewImagesView) {
        this.mView = view
        this.context = context
        compositeSubscription = CompositeSubscription()
    }

    override fun onDetach() {
        if (mView == null)
            return
        mView = null
    }

    override fun getAckSlipsForLBN(tripId: Long, lbn: String) {
        this.tripId = tripId
        ackSlips.clear()
        ackForRecon = RushSearch().whereEqual("lbn", lbn).findSingle(AckForRecon::class.java)

        ackSlips.addAll(ackForRecon!!.ackList.toMutableList())
                //.filter { it.source != AckSource.DISPATCHER.name && it.status != AckStatus.ACCEPTED.name })

        mView?.onAckSlipsFetched(ackSlips)
    }

    override fun onItemSelected(position: Int, status: AckStatus) {
        val selectedItem = ackSlips[position]
        selectedItem.status = status.name
        val selected = selectedList.find { it == selectedItem }
        if(selected == null)
            selectedList.add(selectedItem)


        mView?.setApproveButton(selectedList.size)

    }

    override fun approveImages() {
        if(selectedList.isNotEmpty()) {
            selectedList.forEach { item ->
                val ackSlip = ackSlips.find { it == item }
                ackSlip?.status = item.status
                ackSlip?.save()
            }
            mView?.onImagesApproved(selectedList.size)
        }
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

        if (!storageDir.mkdirs())
            storageDir.mkdir()

        val f = File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        )

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

    private fun createMultipartFormData(signatureFile: File): MultipartBody.Part {
        // create RequestBody instance from file
        val file = File(signatureFile.toString())
        val requestBody = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val filePart = MultipartBody.Part.createFormData("file", file.name, requestBody)
        val requestFile = file
            .asRequestBody(SIGNATURE_MEDIA_TYPE.toMediaTypeOrNull())
        ////Log.d(TAG, "$file")
        return filePart
    }

    override fun uploadFile(currentPhotoPath: File?, lbn: String) {
        val file = File(currentPhotoPath.toString())
        val fileBody =
            file.asRequestBody("image/jpeg".toMediaTypeOrNull())

        mView?.onShowProgress()

        compositeSubscription?.add(
            sortationApiInteractor.getPreSignedUrl()
                .flatMap { res ->
                    sortationApiInteractor.uploadAckSlip("image/jpeg", res.uploadUrl, fileBody)
                        .flatMap { Observable.just(res) }
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    val ackSlip = AckSlipDto(it.key, it.getUrl, lbn)
                    ackSlip.tripId = tripId
                    ackSlip.source = AckSource.DISPATCHER.name
                    ackSlip.status = AckStatus.ACCEPTED.name
                    ackForRecon?.ackList?.add(ackSlip)
                    ackForRecon?.save()
                    ackSlips.add(ackSlip)
                    mView?.onAckSlipUploaded(ackSlip)
                }, { error ->
                    mView?.onFailure(error)
                })
        )
    }

    override fun onItemClicked(position: Int) {
        mView?.startApproveActivity(position, ackForRecon!!.lbn)
    }
}