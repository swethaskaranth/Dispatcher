package com.goflash.dispatch.features.lastmile.settlement.presenter.impl

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import co.uk.rushorm.core.RushSearch
import com.goflash.dispatch.app_constants.SIGNATURE_MEDIA_TYPE
import com.goflash.dispatch.data.AckForRecon
import com.goflash.dispatch.data.AckSlipDto
import com.goflash.dispatch.data.PoaResponseForRecon
import com.goflash.dispatch.data.TripSettlementDTO
import com.goflash.dispatch.di.interactor.SortationApiInteractor
import com.goflash.dispatch.features.lastmile.settlement.presenter.VerifyImagesPresenter
import com.goflash.dispatch.features.lastmile.settlement.view.VerifyImagesView
import com.goflash.dispatch.type.AckSource
import com.goflash.dispatch.type.AckStatus
import com.goflash.dispatch.util.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
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

class VerifyImagesPresenterImpl(private val sortationApiInteractor: SortationApiInteractor) :
    VerifyImagesPresenter {

    private var mView: VerifyImagesView? = null
    private var compositeSubscription: CompositeSubscription? = null

    private var tripId: Long? = null

    private var ackSlipsMap: MutableList<AckForRecon> = mutableListOf()

    private var context: Context? = null

    override fun onAttachView(context: Context, view: VerifyImagesView) {
        this.mView = view
        this.context = context
        compositeSubscription = CompositeSubscription()
    }

    override fun onDetachView() {
        if (mView == null)
            return
        mView = null
        compositeSubscription?.unsubscribe()
        compositeSubscription = null
    }

    override fun setTripId(id: Long) {
        tripId = id
    }

    override fun getData() {
        val ackForRecon = RushSearch().whereEqual("tripId", tripId!!).find(AckForRecon::class.java)

        ackSlipsMap.clear()
        ackSlipsMap.addAll(ackForRecon)
        mView?.onAckSlipsFetched(ackSlipsMap)

        checkIIfAllAckSlipsReviewed()

    }

    override fun onOrderSelected(lbn: String) {
        val ackForRecon = ackSlipsMap.find { it.lbn == lbn }
        if(ackForRecon != null){
           // if(ackForRecon.ackList.any { it.status == null })
                mView?.startReviewActivity(lbn)
        }
    }

    fun checkIIfAllAckSlipsReviewed() {
        var enable = ackSlipsMap.flatMap { it.ackList }.any { it.status == AckStatus.ACCEPTED.name }
        mView?.enableOrDisableProceed(!enable)
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
                    //ackSlip.save()

                    val ackForRecon = ackSlipsMap.find {ack -> ack.lbn == lbn }
                    val list = ackForRecon?.ackList
                    list?.add(ackSlip)
                    ackForRecon?.save()
                    mView?.onAckSlipUploaded()
                }, { error ->
                    mView?.onFailure(error)
                })
        )
    }

    override fun onNext(tripId: Long) {
        val trip =
            RushSearch().whereEqual("tripId", tripId).findSingle(TripSettlementDTO::class.java)
        val deliverySlips = RushSearch().whereChildOf(
            TripSettlementDTO::class.java, "poas",
            trip!!.id
        )
            .find(PoaResponseForRecon::class.java)
        if(deliverySlips.isNullOrEmpty())
            mView?.goToStep3Activity()
        else
            mView?.startAckDeliverySlipReconActivity()
    }
}