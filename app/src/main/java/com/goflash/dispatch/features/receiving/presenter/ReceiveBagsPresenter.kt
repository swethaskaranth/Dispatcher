package com.goflash.dispatch.features.receiving.presenter

import android.content.Context
import android.content.Intent
import com.goflash.dispatch.features.receiving.view.ReceiveBagsView

/**
 *Created by Ravi on 2019-09-08.
 */
interface ReceiveBagsPresenter {

    fun onAttachView(context: Context, view: ReceiveBagsView)

    fun onDetachView()

    fun onTaskResume()

    fun onIntent(intent: Intent)

    fun onBagScanned(barcode: String)
}