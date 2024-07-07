package com.goflash.dispatch.features.rtoReceiving.presenter.impl

import android.content.Context
import android.content.Intent
import co.uk.rushorm.core.RushSearch
import com.goflash.dispatch.api_services.SessionService
import com.goflash.dispatch.app_constants.run_id
import com.goflash.dispatch.data.*
import com.goflash.dispatch.di.interactor.SortationApiInteractor
import com.goflash.dispatch.features.rtoReceiving.presenter.ScanReceiveShipmentPresenter
import com.goflash.dispatch.features.rtoReceiving.view.ScanReceiveShipmentView
import com.goflash.dispatch.util.PreferenceHelper
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription

class ScanReceiveShipmentPresenterImpl(val sortationApiInteractor: SortationApiInteractor) :
    ScanReceiveShipmentPresenter {

    private var mView: ScanReceiveShipmentView? = null
    private var compositeSubscription: CompositeSubscription? = null

    private var inwardRun: InwardRun? = null
    private var inwardRunItems: MutableList<InwardRunItem> = mutableListOf()
    private var inwardRunDisplayItems: MutableList<InwardRunItem> = mutableListOf()

    private var runId: Int = -1

    override fun onAttach(context: Context, view: ScanReceiveShipmentView) {
        this.mView = view
        compositeSubscription = CompositeSubscription()
    }

    override fun sendIntent(intent: Intent) {
        runId = intent.getIntExtra(run_id, -1)
    }

    override fun onDetach() {
        if (mView == null)
            return
        mView = null
        compositeSubscription?.unsubscribe()
        compositeSubscription = null
    }

    override fun getInwardRunItems() {
        if (runId == -1)
            return

        mView?.showProgressBar()
        mView?.disable()
        compositeSubscription?.add(
            sortationApiInteractor.getInwardRunItemsForRunId(runId)
                .flatMap { run ->
                    inwardRun = run
                    val runItems = run.inwardRunItems
                    val mpsRunItems = runItems.filter { it.multipartShipment }
                    val shipmentIds =
                        mpsRunItems.map { it.mpsParentId }.distinct().map { it.toString() }
                    sortationApiInteractor.getShipmentCountForMpsInwardItems(
                        MpsShipmentCountRequest(
                            shipmentIds
                        )
                    )
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ list ->

                    inwardRunItems.clear()
                    inwardRunDisplayItems.clear()

                    inwardRun?.inwardRunItems?.let { items -> inwardRunItems.addAll(items) }

                    groupItems(list)
                    if (inwardRunItems.isNotEmpty())
                        mView?.enableComplete()
                }, {
                    mView?.onFailure(it)
                    mView?.enable()
                })
        )
    }

    override fun onBarcodeScanned(barcode: String) {
        mView?.disable()
        val inwardRunItem =
            inwardRunItems.find { it.wayBillNumber == barcode || it.referenceId == barcode || it.lbn == barcode }

        if (inwardRunItem != null) {
            mView?.onFailure(Throwable("Shipment $barcode already scanned"))
            return
        }
        val mpsShipment = RushSearch()
            .whereEqual("received", false).and()
            .whereEqual("wayBillNumber", barcode)
            .or().whereEqual("lbn", barcode)
            .or().whereEqual("referenceId", barcode)
            .findSingle(ReceivingChildShipmentDTO::class.java)


        val parentShipment = if (mpsShipment != null) RushSearch().whereEqual(
            "shipmentId",
            mpsShipment.parentShipmentId
        )
            .findSingle(ReceivingShipmentDTO::class.java)
        else null

        compositeSubscription?.add(
            (if (parentShipment != null)
                Observable.just(parentShipment)
            else
                sortationApiInteractor.getShipmentDetails(barcode, inwardRun?.partnerId))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ shipment ->

                    if (parentShipment == null && shipment?.isMultiPartShipment == true)
                        if (!shipment.childShipments.any { it.parentShipmentId != null && it.parentShipmentId != 0 }) {
                            shipment.childShipments.forEach {
                                it.parentShipmentId = shipment.shipmentId
                            }
                            shipment.save()
                        }

                    if (shipment?.configuration?.isSortationRequired == true)
                        getSortationBin(shipment)
                    else
                        createInwardRunItem(shipment!!, parentShipment, barcode)

                }, {
                    mView?.onFailure(it)
                    mView?.enable()
                })
        )
    }

    private fun getSortationBin(shipment: ReceivingShipmentDTO) {
        compositeSubscription?.add(
            sortationApiInteractor.getSortationBinV4(SortOrder(shipment.referenceId))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->

                    val scannedPackage = result
                    val scannedOrder = scannedPackage?.scannedOrders?.get(0)
                    if (scannedOrder != null) {
                        if(scannedOrder.isReceivedAtFinalDestination) {
                            mView?.showReceivedMessage()
                            createInwardRunItem(shipment, null, shipment.referenceId)
                        }else
                            mView?.showBinName(scannedPackage, shipment)
                    }
                    else {
                        mView?.onFailure(Throwable("Something went wrong. Please try again later."))
                        mView?.enable()
                    }

                }, { error ->
                    mView?.onFailure(error)
                    mView?.enable()
                })
        )
    }

    private fun createInwardRunItem(
        shipment: ReceivingShipmentDTO,
        parentShipment: ReceivingShipmentDTO?,
        barcode: String
    ) {

        val shipmentId =
            if (shipment.isMultiPartShipment == true)
                shipment.childShipments?.find { it.wayBillNumber == barcode || it.referenceId == barcode || it.lbn == barcode }?.shipmentId
            else
                shipment.shipmentId

        compositeSubscription?.add(
            (if (runId == -1)
                sortationApiInteractor.createInwardRun(CreateInwardRequest(shipment.partnerId))
                    .flatMap { iRun ->
                        runId = iRun.id
                        inwardRun = iRun
                        sortationApiInteractor.createInwardRunItem(
                            runId,
                            CreateInwardRunItemRequest(shipmentId!!, "ACCEPT")
                        )
                    }
            else
                sortationApiInteractor.createInwardRunItem(
                    runId,
                    CreateInwardRunItemRequest(shipmentId!!, "ACCEPT")
                ))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ runItem ->

                    runItem.rejectFlowRequired = shipment.configuration.isRejectFlowRequired

                    runItem.scannedBarcode = barcode
                    inwardRunItems.add(runItem)

                    if (runItem.multipartShipment) {
                        val item =
                            inwardRunDisplayItems.find { it.mpsParentId == runItem.mpsParentId }
                        if (item != null) {
                            item.mpsScannedCount++
                            mView?.onItemRunChanged(item)
                        } else {
                            if (parentShipment != null) {
                                runItem.mpsScannedCount =
                                    parentShipment.childShipments.filter { it.isReceived }.size.plus(
                                        1
                                    )
                                runItem.mpsCount = parentShipment.childShipments.size
                                runItem.scannedBarcode = parentShipment.lbn
                            } else {
                                val shipmentLocal =
                                    RushSearch().whereEqual("wayBillNumber", barcode)
                                        .or().whereEqual("lbn", barcode)
                                        .or().whereEqual("referenceId", barcode)
                                        .findSingle(ReceivingChildShipmentDTO::class.java)


                                val parent = if (shipmentLocal != null) RushSearch().whereEqual(
                                    "shipmentId",
                                    shipmentLocal.parentShipmentId
                                )
                                    .findSingle(ReceivingShipmentDTO::class.java)
                                else null

                                if (parent != null) {
                                    runItem.mpsScannedCount =
                                        parent.childShipments.filter { it.isReceived }.size.plus(1)
                                    runItem.mpsCount = parent.childShipments.size
                                    runItem.scannedBarcode = parent.lbn
                                }
                            }
                            inwardRunDisplayItems.add(runItem)
                            mView?.onInwardRunItemAdded(runItem)
                        }
                    } else {
                        inwardRunDisplayItems.add(runItem)
                        mView?.onInwardRunItemAdded(runItem)
                    }

                    mView?.enableComplete()
                    mView?.enable()
                    mView?.updateScannedItemCount(inwardRunDisplayItems.size)

                }, {
                    mView?.onFailure(it)
                    mView?.enable()
                })
        )

    }

    private fun groupItems(list: List<MpsShipmentCountDTO>) {
        inwardRunDisplayItems.clear()
        inwardRunDisplayItems.addAll(inwardRunItems)

        val mpsShipments =
            inwardRunItems.filter { it.multipartShipment && it.mpsParentId != null }

        if (mpsShipments.isNotEmpty()) {
            val mpsLocalShipments = RushSearch()
                .whereIN(
                    "parentShipmentId",
                    mpsShipments.map { it.mpsParentId }.map { it.toString() }.toMutableList()
                )
                .find(ReceivingChildShipmentDTO::class.java)

            inwardRunDisplayItems.removeAll(mpsShipments)

            if (mpsShipments.isNotEmpty()) {
                val groupedShipments = mpsShipments.groupBy { it.mpsParentId }
                for (entry in groupedShipments.entries) {
                    val displayShipment = inwardRunItems.first { it.mpsParentId == entry.key }
                    val scannedShipments =
                        mpsLocalShipments.filter { it.parentShipmentId == entry.key && !inwardRunItems.any { runItem -> it.lbn == runItem.lbn } }
                    val shipmentCount =
                        list.find { it.parentShipmentId == displayShipment.mpsParentId.toString() }?.childShipmentCount
                    displayShipment.mpsCount = shipmentCount ?: 0
                    displayShipment.mpsScannedCount = (groupedShipments[entry.key]?.size ?: 0).plus(
                        scannedShipments.filter { it.isReceived }.size
                    )
                    displayShipment.scannedBarcode = displayShipment.mpsParentLbn.toString()
                    inwardRunDisplayItems.add(displayShipment)
                }
            }
        }
        mView?.onInwardRunItemsFetched(inwardRunDisplayItems)
        mView?.enable()
    }

    override fun getExceptionReasons(position: Int) {
        val runItem = inwardRunItems[position]
        compositeSubscription?.add(
            sortationApiInteractor.getExceptionReasonsForShipment(
                runItem.shipmentId!!
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    mView?.onExceptionsFetched(
                        runItem.wayBillNumber ?: runItem.lbn,
                        runItem.shipmentStatus,
                        it.exceptionReasons
                    )

                }, {
                    mView?.onFailure(it)

                })
        )
    }

    override fun onReasonSelected(status: String, wayBillNumber: String, exceptions: List<String>) {
        val runItem =
            inwardRunItems.find { it.wayBillNumber == wayBillNumber || it.lbn == wayBillNumber }
        val request = UpdateInwardRunItemRequest(runItem?.shipmentId!!, exceptions, status)
        compositeSubscription?.add(
            sortationApiInteractor.updateInwardRunItem(
                runId,
                runItem.id,
                request
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    runItem.status = status
                    mView?.onStatusUpdated(runItem)
                    mView?.enableComplete()

                }, {
                    mView?.onFailure(it)
                })
        )
    }

    override fun completeInwardRun() {
        compositeSubscription?.add(
            sortationApiInteractor.completeInwardRun(
                runId,
                CompleteInwardRunRequest(
                    "COMPLETED",
                    SessionService.userId,
                    SessionService.username,
                    PreferenceHelper.assignedAssetId.toInt()
                )
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    mView?.onSuccess(runId)
                }, {
                    mView?.onFailure(it)
                })
        )
    }

    override fun getMpsRunItems(position: Int) {
        val parentId = inwardRunDisplayItems[position].mpsParentId
        mView?.onMpsRunItemsFetched(
            runId,
            inwardRunDisplayItems[position].mpsCount,
            inwardRunItems.filter { it.mpsParentId == parentId })
    }

    override fun onSortationComplete(shipment: ReceivingShipmentDTO) {
        createInwardRunItem(shipment, null, shipment.referenceId)
    }
}