package com.goflash.dispatch.presenter

import android.content.Context
import android.content.Intent
import com.goflash.dispatch.data.PackageDto
import com.goflash.dispatch.presenter.views.CancelledRowView
import com.goflash.dispatch.presenter.views.CancelledView

interface CancelledPresenter{

    fun onAttachView(context: Context, view: CancelledView)

    fun onDetachView()

    fun sendIntent(intent: Intent)

    fun onBarcodeScanned(barcode : String)

    fun onBinScan(PackageDto: PackageDto)

    //fun dispatchOrders()

    fun getNonDispatchableOrders()

    fun onBindCanceeldRowView(position : Int, cancelledRowView: CancelledRowView)

    fun getCount() : Int

}