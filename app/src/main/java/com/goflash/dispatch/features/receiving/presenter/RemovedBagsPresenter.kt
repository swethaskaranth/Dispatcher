package com.goflash.dispatch.features.receiving.presenter

import android.content.Context
import android.content.Intent
import com.goflash.dispatch.features.bagging.view.BagRowView
import com.goflash.dispatch.features.receiving.view.RemoveBagsView

interface RemovedBagsPresenter {

    fun onAttachView(context: Context , view : RemoveBagsView)

    fun onDeatchView()

    fun getCount() : Int

    fun onBindBagdRowView(position : Int, holder : BagRowView)

    fun onBarcodeScanned(barcode : String)

    fun undoRemove()

    fun getUpdatedBagList()

    fun onIntent(intent: Intent)
}