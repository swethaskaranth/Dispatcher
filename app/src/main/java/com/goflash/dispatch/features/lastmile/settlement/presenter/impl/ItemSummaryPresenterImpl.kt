package com.goflash.dispatch.features.lastmile.settlement.presenter.impl

import android.content.Context
import android.content.Intent
import co.uk.rushorm.core.RushCore
import co.uk.rushorm.core.RushSearch
import com.goflash.dispatch.app_constants.*
import com.goflash.dispatch.data.*
import com.goflash.dispatch.di.interactor.SortationApiInteractor
import com.goflash.dispatch.features.lastmile.settlement.presenter.ItemSummaryPresenter
import com.goflash.dispatch.features.lastmile.settlement.view.ItemSummaryView
import com.goflash.dispatch.type.ReconStatus
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription
import java.io.File

class ItemSummaryPresenterImpl(private val sortationApiInteractor: SortationApiInteractor) :
    ItemSummaryPresenter {

    private var mView: ItemSummaryView? = null

    private var compositeSubscription: CompositeSubscription? = null

    private var shipmentId: String? = null
    private var partialDelivery = false

    private var tripId: Long? = null

    private var items: MutableList<Item> = mutableListOf()

    private var returnedItems: MutableList<Item> = mutableListOf()

    override fun onAttachView(context: Context, view: ItemSummaryView) {
        this.mView = view
        compositeSubscription = CompositeSubscription()

    }

    override fun setShipmentId(id: String, tripId: Long, partialDelivery: Boolean) {
        shipmentId = id
        this.tripId = tripId
        this.partialDelivery = partialDelivery
        getItems()
    }

    override fun getItems() {
        val trip =
            RushSearch().whereEqual("tripId", tripId!!).findSingle(TripSettlementDTO::class.java)

        val shipments =
            RushSearch().whereChildOf(TripSettlementDTO::class.java, "returnShipment", trip!!.id)
                .find(ReturnShipmentDTO::class.java).filter { !it.isScanned }.toMutableList()
        shipments.removeAll { it.shipmentId == shipmentId }
        if (shipments.isEmpty())
            mView?.setButtonToProceed()

        items = RushSearch().whereEqual("shipmentId", shipmentId).find(Item::class.java)

        returnedItems.clear()

        val barcodedItems = items.filter { !it.barcode.isNullOrEmpty()  }

        val nonNullItems = barcodedItems.filter { it.ucode != null && it.batchNumber != null }
        val groupByUcode = barcodedItems.sortedBy { it.displayName }
            .groupBy { Triple(it.ucode, it.displayName, it.batchNumber) }
        groupByUcode.keys.map {
            val ucodeItems = groupByUcode[it]
            val item = ucodeItems!![0]
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

        val nonBarcodedItems = items.filter { it.barcode.isNullOrEmpty()  }
        val groupNonBarcoded = nonBarcodedItems.sortedBy { it.displayName }
            .groupBy {  Triple(it.ucode, it.displayName, it.batchNumber) }

        groupNonBarcoded.keys.map {
            val ucodeItems = groupNonBarcoded[it]
            val item = ucodeItems!![0]
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

        if(partialDelivery)
            mView?.enableOrDisableProceed(returnedItems.any {
                it.reconAcceptedQuantity != it.returnedQuantity
            })
        else
            mView?.enableOrDisableProceed(!returnedItems.any {
            it.reconRejectedQuantity + it.reconAcceptedQuantity >= 1
        })

        mView?.onItemsFetched(returnedItems)
    }

    override fun reviewItem(position: Int) {
        val item = returnedItems[position]

        val intent = Intent()
        intent.putExtra(display_name, item.displayName)
        intent.putExtra(ucode, item.ucode)
        intent.putExtra(batch_number, item.batchNumber)
        intent.putExtra(item_id, item.itemId)
        intent.putExtra(shipment_id, item.shipmentId)
        if(!item.barcode.isNullOrEmpty()) {
            mView?.goToReviewItemActivity(intent)
        }else{
            mView?.goToReceiveItemActivity(intent)
        }


    }

    override fun onScanNext(scan: Boolean) {
        val shipment = RushSearch().whereEqual("shipmentId", shipmentId)
            .findSingle(ReturnShipmentDTO::class.java)
        shipment.isScanned = true
        shipment.save()

        if (scan)
            mView?.goToScanShipmentActivity()
        else{
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

                    mView?.onImageUploaded(currentPhotoPath.toString(), res.key)

                }, { error ->
                    mView?.onFailure(error)
                })
        )
    }

    override fun getItemCount(): Int = items.size

    override fun removeReconImage(key: String) {
        val reconImage = RushSearch().whereEqual("pathString", key).find(ReconImageDTO::class.java)
        RushCore.getInstance().delete(reconImage)
    }

    override fun deleteImages(shipmentId: String) {
        val reconImage = RushSearch().whereEqual("shipmentId", shipmentId).find(ReconImageDTO::class.java)
        RushCore.getInstance().delete(reconImage)
    }

}