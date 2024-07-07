package com.goflash.dispatch.features.lastmile.settlement.presenter

import android.content.Context
import com.goflash.dispatch.features.lastmile.settlement.view.ItemSummaryView
import java.io.File

interface ItemSummaryPresenter {

    fun onAttachView(context: Context, view : ItemSummaryView)

    fun onDetachView()

    fun setShipmentId(id: String, tripId : Long, partialDelivery: Boolean)

    fun getItems()

    fun reviewItem(position : Int)

    fun onScanNext(scan : Boolean)

    fun uploadFile(currentPhotoPath: File?)

    fun getItemCount(): Int

    fun removeReconImage(key: String)

    fun deleteImages(shipmentId: String)

}