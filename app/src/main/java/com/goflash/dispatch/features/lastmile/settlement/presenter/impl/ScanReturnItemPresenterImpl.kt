package com.goflash.dispatch.features.lastmile.settlement.presenter.impl

import android.content.Context
import co.uk.rushorm.core.RushSearch
import com.goflash.dispatch.data.Item
import com.goflash.dispatch.data.ReturnShipmentDTO
import com.goflash.dispatch.di.interactor.SortationApiInteractor
import com.goflash.dispatch.features.lastmile.settlement.presenter.ScanReturnItemPresenter
import com.goflash.dispatch.features.lastmile.settlement.view.ScanReturnItemView
import com.goflash.dispatch.type.ReconStatus
import rx.subscriptions.CompositeSubscription

class ScanReturnItemPresenterImpl(private val sortationApiInteractor: SortationApiInteractor) :
    ScanReturnItemPresenter {

    private var mView: ScanReturnItemView? = null

    private var compositeSubscription: CompositeSubscription? = null

    private var shipmentId: String? = null

    private var items: MutableList<Item> = mutableListOf()

    private var scannedItems: MutableList<Item> = mutableListOf()

    private var isPartialDelivery = false

    private var returnedItems: MutableList<Item> = mutableListOf()

    override fun onAttachView(context: Context, view: ScanReturnItemView) {
        this.mView = view
        compositeSubscription = CompositeSubscription()
    }

    override fun setShipmentId(id: String, partialDelivery: Boolean) {
        shipmentId = id
        isPartialDelivery = partialDelivery
    }

    override fun getItems() {
        items = RushSearch().whereEqual("shipmentId", shipmentId).find(Item::class.java)
        scannedItems.addAll(items.filter { it.isItemScanned })

        val returnItems = mutableListOf<Item>()
        returnItems.addAll(RushSearch().whereEqual("shipmentId", shipmentId).find(Item::class.java))
        val groupByUcode = returnItems.sortedBy { it.displayName }
            .groupBy { Triple(it.ucode, it.displayName, it.batchNumber) }

        groupByUcode.keys.map {
            val ucodeItems = groupByUcode[it]
            val item = ucodeItems!![0]
            item.returnedQuantity =
                ucodeItems.map { it.returnedQuantity }.reduce { acc, i -> acc.plus(i) }

            returnedItems.add(item)
        }


        mView?.onScannedItemsFetched(scannedItems)

        mView?.setItemCount(scannedItems.size, items.size)
    }

    override fun onBarcodeScanned(barcode: String) {
        if (scannedItems.any { it.barcode == barcode }) {
            mView?.onFailure(Throwable("Item already scanned."))
            return
        }

        val item = items.find { it.barcode == barcode }

        if(isPartialDelivery && item != null){
            val scannedUcode = scannedItems.filter { it.ucode == item.ucode && it.batchNumber == item.batchNumber}
            val returnedQuantity = returnedItems.filter { it.ucode == item.ucode && it.batchNumber == item.batchNumber }.map { it.returnedQuantity }
            if(scannedUcode.size == returnedQuantity.size) {
                mView?.onFailure(Throwable("The accepted quantity cannot exceed picked up quantity"))
                return
            }
        }

        if (item != null) {
            item.isItemScanned = true
            item.save()

            scannedItems.removeAll { it.itemId == item.itemId }

            scannedItems.add(0,item)

            mView?.onScannedItemsFetched(scannedItems)
            mView?.setItemCount(scannedItems.size, items.size)

        } else{
            val wrongItem = items.firstOrNull{ it.returnReason == "Wrong item received" && !it.isItemScanned }
            if(wrongItem != null){
                wrongItem.isItemScanned = true
                wrongItem.save()

                scannedItems.removeAll { it.itemId == wrongItem.itemId }
                scannedItems.add(wrongItem)
                mView?.onScannedItemsFetched(scannedItems)
                mView?.setItemCount(scannedItems.size, items.size)
            }else{
                mView?.onFailure(Throwable("Invalid Barcode. Please scan valid Barcode."))
            }

        }


    }

    override fun setAcceptReject(position: Int, reconStatus: ReconStatus, reason: String, rejectRemarks: String) {
        val item = RushSearch().whereEqual("itemId", scannedItems[position].itemId)
            .findSingle(Item::class.java)
        if (item != null) {
            item.reconStatus = reconStatus.name
            item.reconStatusReason = reason
            item.isItemScanned = true
            item.reconRemark = rejectRemarks
            item.save()
        }

        scannedItems.removeAll { it.itemId == item.itemId }

        scannedItems.add(item)
        mView?.onScannedItemsFetched(scannedItems)
        mView?.setItemCount(scannedItems.size, items.size)

    }

    override fun onDetachView() {
        if (mView == null)
            return
        mView = null
        compositeSubscription?.unsubscribe()
        compositeSubscription = null
    }
}