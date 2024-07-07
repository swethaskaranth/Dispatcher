package com.goflash.dispatch.features.receiving.presenter

import android.content.Context
import com.goflash.dispatch.features.receiving.view.VehicleScanView
import okhttp3.MultipartBody
import java.io.File

/**
 *Created by Ravi on 2019-09-08.
 */
interface VehicleScanPresenter {

    fun onAttachView(context: Context, view: VehicleScanView)

    fun onDetachView()

    fun verifyAndUploadImage(file: MultipartBody.Part, vehicleId: String)

    fun createMultipartFormData(signatureFile: File): MultipartBody.Part

    fun uploadFile(currentPhotoPath: File?, id: String)

    fun verifyVehicleSeal(vehicleId: String, tripId : String?)
}