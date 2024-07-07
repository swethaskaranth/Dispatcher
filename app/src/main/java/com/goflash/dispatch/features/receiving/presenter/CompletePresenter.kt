package com.goflash.dispatch.features.receiving.presenter

import android.content.Context
import android.content.Intent
import com.goflash.dispatch.features.receiving.view.CompleteScanView

/**
 *Created by Ravi on 2019-09-08.
 */
interface CompletePresenter {

    fun onAttachView(context: Context, view: CompleteScanView)

    fun onDetachView()

    fun getSealrequired() : Boolean

    fun verifyVehicleSeal()

    fun onIntent(intent: Intent)

    fun onTaskResume()

    fun onBagScanned(barcode: String)
}