package com.goflash.dispatch.features.dispatch.presenter

import android.content.Context
import com.goflash.dispatch.features.bagging.view.BagRowView
import com.goflash.dispatch.features.dispatch.view.ScannedBagsView

interface ScannedBagsPresenter {

    fun onAttachView(context: Context , view : ScannedBagsView)

    fun onDeatchView()

    fun getCount() : Int

    fun onBindBagdRowView(position : Int, holder : BagRowView)

    fun onBarcodeScanned(barcode : String)

    fun undoRemove()

    fun getUpdatedBagList(addBag: Boolean)



}