package com.goflash.dispatch.features.lastmile.settlement.presenter.impl

import android.content.Context
import android.content.Intent
import android.util.Log
import co.uk.rushorm.core.RushSearch
import com.goflash.dispatch.app_constants.*
import com.goflash.dispatch.data.*
import com.goflash.dispatch.di.interactor.SortationApiInteractor
import com.goflash.dispatch.features.lastmile.settlement.presenter.ReceiveItemPresenter
import com.goflash.dispatch.features.lastmile.settlement.view.ReceiveItemView
import com.goflash.dispatch.type.AckSource
import com.goflash.dispatch.type.AckStatus
import com.goflash.dispatch.type.ReconStatus
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

class ReceiveItemPresenterImpl(private val sortationApiInteractor: SortationApiInteractor) :
    ReceiveItemPresenter {

    private var mView: ReceiveItemView? = null

    private var compositeSubscription: CompositeSubscription? = null

    private var shipmentId: String? = null
    private var partialDelivery = false

    private var items: MutableList<Item> = mutableListOf()

    private var returnedItems: MutableList<Item> = mutableListOf()

    private var tripId: Long? = null

    override fun onAttachView(context: Context, view: ReceiveItemView) {
        this.mView = view
        compositeSubscription = CompositeSubscription()
    }

    override fun sendIntent(intent: Intent?) {
        val itemUcode = intent?.getStringExtra(ucode)
        val batch = intent?.getStringExtra(batch_number)
        val name = intent?.getStringExtra(display_name)
        val shipmentId = intent?.getStringExtra(shipment_id)
        val itemId = intent?.getIntExtra(item_id, 0)
        partialDelivery = intent?.getBooleanExtra(partial_delivery, false)?: false

        getItems(itemId ?: 0, itemUcode, batch, name, shipmentId!!)
    }

    override fun setShipmentId(id: String, tripId : Long, partial: Boolean) {
        shipmentId = id
        this.tripId = tripId
        partialDelivery = partial
    }

    override fun getItems(itemId: Int,
                          ucode: String?,
                          batch: String?,
                          name: String?,
                          shipmentId: String) {
        val trip =
            RushSearch().whereEqual("tripId", tripId!!).findSingle(TripSettlementDTO::class.java)

        val shipments =
            RushSearch().whereChildOf(TripSettlementDTO::class.java, "returnShipment", trip!!.id)
                .find(ReturnShipmentDTO::class.java).filter { !it.isScanned }.toMutableList()
        shipments.removeAll { it.shipmentId == shipmentId }
        if (shipments.isEmpty())
            mView?.setButtonToProceed()

        items = RushSearch().whereEqual("shipmentId", shipmentId)
            /*.and()
            .whereEqual("ucode", ucode).and()
            .whereEqual("displayName", name).and()
            .whereEqual("batchNumber", batch)*/
            .find(Item::class.java)

        items = items.filter { it.ucode ==ucode && it.batchNumber == batch && it.displayName == name}.toMutableList()

        returnedItems.clear()

        val nonNullItems = items.filter { it.ucode != null && it.batchNumber != null }
        val groupByUcode = items.sortedBy { it.displayName }
            .groupBy { Triple(it.ucode, it.displayName, it.batchNumber) }
        groupByUcode.keys.map {
            val ucodeItems = groupByUcode[it]
            val item = ucodeItems!![0]
            item.quantity =
                ucodeItems.map { it.quantity }.reduce { x, y -> x.plus(y) }
            item.returnRaisedQuantity =
                ucodeItems.map { it.returnRaisedQuantity }.reduce { x, y -> x.plus(y) }
            item.returnedQuantity =
                ucodeItems.map { it.returnedQuantity }.reduce { acc, i -> acc.plus(i) }

            item.reconAcceptedQuantity =
                ucodeItems.filter { it.reconStatus == ReconStatus.ACCEPT.name }.size
            item.reconRejectedQuantity =
                ucodeItems.filter { it.reconStatus == ReconStatus.REJECT.name }.size

            returnedItems.add(item)

        }

        /*val nullItems = items.filter { it.ucode == null || it.batchNumber == null }
        val groupByNa
        for (item in nullItems) {
            if (item.reconStatus == ReconStatus.ACCEPT.name)
                item.reconAcceptedQuantity = item.returnedQuantity
            else if (item.reconStatus == ReconStatus.REJECT.name)
                item.reconRejectedQuantity = item.returnedQuantity
        }
        returnedItems.addAll(nullItems)*/

       checkToEnableProceed()

        mView?.onItemsFetched(returnedItems)
    }

    override fun acceptMedicine(
        itemId: Int,
        ucode: String?,
        display: String,
        batch: String?,
        quantity: Int,
        reason: String
    ) {
        if(returnedItems.isNotEmpty() && returnedItems.any { (partialDelivery && it.reconAcceptedQuantity != 0 && it.reconAcceptedQuantity != it.returnedQuantity) || it.reconAcceptedQuantity + it.reconRejectedQuantity > it.quantity?: 0 }) {
            val item = returnedItems.filter { it.itemId == itemId && it.ucode == ucode && it.batchNumber == batch }[0]
            if (ucode == null || batch == null) {
                val localItem = RushSearch().whereEqual("itemId", itemId)
                    .findSingle(Item::class.java)
                localItem.reconAcceptedQuantity = 0
                localItem.reconStatus = null
                localItem.reconRejectedQuantity = 0
                localItem.reconStatusReason = null
                localItem.save()
            }
            else {
                val localItems =  RushSearch().whereEqual("ucode", ucode)
                    .and().whereEqual("batchNumber", batch)
                    .and().whereEqual("displayName", display)
                    .and().whereEqual("shipmentId", shipmentId).find(Item::class.java)
                for(localItem in localItems){
                    localItem.reconAcceptedQuantity = 0
                    localItem.reconStatus = null
                    localItem.reconRejectedQuantity = 0
                    localItem.reconStatusReason = null
                    localItem.save()
                }
            }
            item.reconAcceptedQuantity = 0
            mView?.onItemsFetched(returnedItems)
            mView?.onFailure(Throwable("The total accepted and rejected meds can not be greater than the total quantity"))
            mView?.enableOrDisableProceed(false)
            return
        }

        if (ucode == null || batch == null) {
            val selectedItem = RushSearch().whereEqual("itemId", itemId)
                .findSingle(Item::class.java)
            selectedItem?.reconStatus = ReconStatus.ACCEPT.name
            selectedItem?.reconAcceptedQuantity = quantity
            selectedItem?.reconStatusReason = reason

            selectedItem.save()

        } else {
            val selectedItems = RushSearch().whereEqual("ucode", ucode)
                .and().whereEqual("batchNumber", batch)
                .and().whereEqual("displayName", display)
                .and().whereEqual("shipmentId", shipmentId)
               // .and().whereIsNull("reconStatus")
                .find(Item::class.java)

            selectedItems.sortBy { it.reconStatus }

            var selectedQuantity = quantity

            for (i in 0 until selectedItems.size) {
                val selectedItem = selectedItems[i]
                selectedItem.reconAcceptedQuantity =
                    if (selectedQuantity > selectedItem.quantity ?: 0)
                        selectedItem.quantity ?: 0
                    else
                        selectedQuantity
                selectedQuantity -= selectedItem.reconAcceptedQuantity ?: 0
                if (selectedQuantity < 0)
                    selectedQuantity = 0

                if (selectedItem.reconAcceptedQuantity > 0) {
                    selectedItem.reconStatus = ReconStatus.ACCEPT.name
                    selectedItem.reconStatusReason = reason
                }/*else {
                    selectedItem.reconStatus = null
                    selectedItem.reconStatusReason = null
                }*/

                selectedItem.save()
            }
        }
        checkToEnableProceed()
    }

    override fun rejectMedicine(
        itemId: Int,
        ucode: String?,
        display: String,
        batch: String?,
        quantity: Int,
        reason: String,
        rejectRemarks: String
    ) {
        if(returnedItems.isNotEmpty() && returnedItems.any { it.reconAcceptedQuantity + it.reconRejectedQuantity > it.quantity?: 0 }) {

            val item = returnedItems.filter { it.itemId == itemId && it.ucode == ucode && it.batchNumber == batch }[0]
             if (ucode == null || batch == null) {
                 val localItem = RushSearch().whereEqual("itemId", itemId)
                     .findSingle(Item::class.java)
                 localItem.reconAcceptedQuantity = 0
                 localItem.reconStatus = null
                 localItem.reconRejectedQuantity = 0
                 localItem.reconStatusReason = null
                 localItem.reconRemark = null
                 localItem.save()
             }
            else {
               val localItems =  RushSearch().whereEqual("ucode", ucode)
                     .and().whereEqual("batchNumber", batch)
                     .and().whereEqual("displayName", display)
                     .and().whereEqual("shipmentId", shipmentId).find(Item::class.java)
                 for(localItem in localItems){
                     localItem.reconAcceptedQuantity = 0
                     localItem.reconStatus = null
                     localItem.reconRejectedQuantity = 0
                     localItem.reconStatusReason = null
                     localItem.reconRemark = null
                     localItem.save()
                 }
             }
            item.reconRejectedQuantity = 0
            mView?.onItemsFetched(returnedItems)
            mView?.onFailure(Throwable("The total accepted and rejected meds can not be greater than the total quantity"))
            mView?.enableOrDisableProceed(false)
            return
        }

        if (ucode == null || batch == null) {
            val selectedItem = RushSearch().whereEqual("itemId", itemId)
                .findSingle(Item::class.java)
            selectedItem?.reconStatus = ReconStatus.REJECT.name
            selectedItem?.reconRejectedQuantity = quantity
            selectedItem?.reconStatusReason = reason
            selectedItem?.reconRemark = rejectRemarks

            selectedItem.save()

        } else {
            val selectedItems = RushSearch().whereEqual("ucode", ucode)
                .and().whereEqual("batchNumber", batch)
                .and().whereEqual("displayName", display)
                .and().whereEqual("shipmentId", shipmentId)
                //.and().whereIsNull("reconStatus")
                .find(Item::class.java)


            var selectedQuantity = quantity

            for (i in 0 until selectedItems.size) {
                val selectedItem = selectedItems[i]
                selectedItem.reconRejectedQuantity =
                    if (selectedQuantity > (selectedItem.quantity).minus(selectedItem.reconAcceptedQuantity))
                        (selectedItem.quantity).minus(selectedItem.reconAcceptedQuantity)
                    else
                        selectedQuantity
                selectedQuantity -= selectedItem.reconRejectedQuantity ?: 0
                if (selectedQuantity < 0)
                    selectedQuantity = 0

                if (selectedItem.reconAcceptedQuantity <= 0 && selectedItem.reconRejectedQuantity > 0) {
                    selectedItem.reconStatus = ReconStatus.REJECT.name
                    selectedItem.reconStatusReason = reason
                    selectedItem.reconRemark = rejectRemarks
                }

                selectedItem.save()
            }
        }
        checkToEnableProceed()
    }

    private fun checkToEnableProceed(){
        val pattern =  Pattern.compile("^([A-Za-z][A-Za-z0-9-_ ]*)$")
        if(returnedItems.isNotEmpty() && returnedItems.any { it.reconAcceptedQuantity + it.reconRejectedQuantity > it.quantity?: 0 }) {
            mView?.onFailure(Throwable("The total accepted and rejected meds can not be greater than the total quantity"))
            return
        }
       mView?.enableOrDisableProceed(returnedItems.any { (partialDelivery && it.reconAcceptedQuantity != it.returnedQuantity)
               || (it.reconRejectedQuantity + it.reconAcceptedQuantity <1 ||
               (it.reconRemark?.isNotEmpty() == true && !pattern.matcher(it.reconRemark).find())) })
    }

    override fun onScanNext(scan : Boolean) {
        val shipment = RushSearch().whereEqual("shipmentId",shipmentId).findSingle(ReturnShipmentDTO::class.java)
        shipment.isScanned = true
        shipment.save()

        if (scan)
            mView?.goToScanShipmentActivity()
        else {
            val fmShipments = RushSearch().whereEqual("tripId", tripId!!).find(FmPickedShipment::class.java)
            if(fmShipments.isNullOrEmpty()) {
                val ackSlips = RushSearch().whereEqual("tripId", tripId!!).find(AckSlipDto::class.java)
                if(ackSlips.isNullOrEmpty()) {
                    val trip =
                        RushSearch().whereEqual("tripId", tripId!!).findSingle(TripSettlementDTO::class.java)
                    val deliverySlips = RushSearch().whereChildOf(
                        TripSettlementDTO::class.java, "poas",
                        trip!!.id
                    )
                        .find(PoaResponseForRecon::class.java)
                    if(deliverySlips.isNullOrEmpty())
                        mView?.goToStep3Activity()
                    else
                        mView?.startAckDeliverySlipReconActivity()
                }
                else
                    mView?.startVerifyImageActivity()
            }
            else
                mView?.startReceiveFmPickupActivity()
        }
    }

    override fun onDetachView() {
        if (mView == null)
            return
        mView = null
        compositeSubscription?.unsubscribe()
        compositeSubscription = null
    }

    override fun uploadFile(currentPhotoPath: File?) {
        val file = File(currentPhotoPath.toString())
        val fileBody =
            file.asRequestBody("image/jpeg".toMediaTypeOrNull())

        mView?.onShowProgress()

        compositeSubscription?.add(
            sortationApiInteractor.getPreSignedUrl("reconImage")
                .flatMap { res ->
                    sortationApiInteractor.uploadAckSlip("image/jpeg", res.uploadUrl, fileBody)
                        .flatMap { Observable.just(res) }
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({res ->
                    val reconImage  = ReconImageDTO(mutableListOf(res.key), shipmentId!!)
                    reconImage.save()

                    mView?.onImageUploaded()

                }, { error ->
                    mView?.onFailure(error)
                })
        )
    }
}