package com.goflash.dispatch.features.receiving.presenter

import android.content.Context
import android.content.Intent
import com.goflash.dispatch.features.receiving.view.AddBagView

/**
 *Created by Ravi on 2019-09-08.
 */
interface AddBagPresenter {

    fun onAttachView(context: Context, view: AddBagView)

    fun onDetachView()

    fun onTaskResume()

    fun onIntent(intent: Intent)

    fun onBagScanned(barcode: String)

}