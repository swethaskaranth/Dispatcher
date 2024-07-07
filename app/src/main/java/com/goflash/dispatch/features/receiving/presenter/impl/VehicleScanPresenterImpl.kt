package com.goflash.dispatch.features.receiving.presenter.impl

import android.content.Context
import com.goflash.dispatch.app_constants.SIGNATURE_MEDIA_TYPE
import com.goflash.dispatch.di.interactor.SortationApiInteractor
import com.goflash.dispatch.features.receiving.presenter.VehicleScanPresenter
import com.goflash.dispatch.features.receiving.view.VehicleScanView
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription
import java.io.File

/**
 *Created by Ravi on 2019-09-08.
 */
class VehicleScanPresenterImpl(val sortationApiInteractor: SortationApiInteractor): VehicleScanPresenter {

    private var view: VehicleScanView? = null
    private var compositeSubscription : CompositeSubscription? = null

    override fun onAttachView(context: Context, view: VehicleScanView) {
        this.view = view
        compositeSubscription = CompositeSubscription()
    }

    override fun onDetachView() {
        if(view != null)
            view = null

        compositeSubscription?.unsubscribe()
        compositeSubscription = null

    }

    /**
     * When vehicle seal is broken and upload the image to server
     */
    override fun verifyAndUploadImage(file: MultipartBody.Part, vehicleId: String) {

        view?.onShowProgress()

        compositeSubscription?.add(sortationApiInteractor.uploadImage(file, vehicleId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                view?.onSuccess(it)
            },{error ->
                view?.onFailure(error)
            }))
    }

    /**
     * Scan vehicle seal and call server
     */
    override fun verifyVehicleSeal(vehicleId: String, tripId : String?) {

        view?.onShowProgress()

        compositeSubscription?.add(sortationApiInteractor.verifiyVehicleSealScan(vehicleId, tripId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                view?.onSuccess(it)
            },{error ->
                view?.onFailure(error)
            }))
    }

    /**
     * create file form data
     * @param signatureFile [ signature file]
     * */
    override fun createMultipartFormData(signatureFile: File): MultipartBody.Part {
        // create RequestBody instance from file
        val file = File(signatureFile.toString())
        val requestFile = file
            .asRequestBody(SIGNATURE_MEDIA_TYPE.toMediaTypeOrNull())
        ////Log.d(TAG, "$file")
        return MultipartBody.Part.createFormData("file", file.name, requestFile)
    }

    override fun uploadFile(currentPhotoPath: File?, id: String){

        val fileBody = createMultipartFormData(currentPhotoPath!!)

        verifyAndUploadImage(fileBody, id)
    }
}