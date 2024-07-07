package com.goflash.dispatch.features.lastmile.settlement.presenter

import android.content.Context
import android.content.Intent
import com.goflash.dispatch.features.lastmile.settlement.view.ReceiveItemView
import java.io.File

interface ReceiveItemPresenter {

    fun onAttachView(context: Context, view : ReceiveItemView)

    fun onDetachView()

    fun getItems(itemId: Int,
                 ucode: String?,
                 batch: String?,
                 name: String?,
                 shipmentId: String)

    fun sendIntent(intent : Intent?)

    fun setShipmentId(id: String, tripId : Long, partialDelivery: Boolean)

    fun acceptMedicine(itemId : Int,ucode : String?, display : String, batch : String?,  quantity : Int, reason : String)

    fun rejectMedicine(itemId : Int,ucode : String?, display : String, batch : String?,  quantity : Int, reason : String, rejectRemarks: String)

    fun onScanNext(scan : Boolean)

    fun uploadFile(currentPhotoPath: File?)
}