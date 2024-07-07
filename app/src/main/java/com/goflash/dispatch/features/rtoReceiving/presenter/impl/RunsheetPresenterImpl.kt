package com.goflash.dispatch.features.rtoReceiving.presenter.impl

import android.content.Context
import android.content.Intent
import com.goflash.dispatch.app_constants.run_id
import com.goflash.dispatch.data.*
import com.goflash.dispatch.di.interactor.SortationApiInteractor
import com.goflash.dispatch.features.rtoReceiving.presenter.RunsheetPresenter
import com.goflash.dispatch.features.rtoReceiving.view.RunsheetView
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription

class RunsheetPresenterImpl(val sortationApiInteractor: SortationApiInteractor) :
    RunsheetPresenter {

    private var mView: RunsheetView? = null
    private var compositeSubscription: CompositeSubscription? = null

    private var runId = -1
    private var inwardRun: InwardRun? = null

    private var runItemsByGroup: Map<String?, List<InwardRunItem>> = HashMap()

    private var partnerList: MutableList<PartnerNameDTO> = mutableListOf()
    private var inwardRunItems: MutableList<InwardRunItem> = mutableListOf()
    private var inwardRunDisplayItems: MutableList<InwardRunItem> = mutableListOf()

    override fun onAttach(context: Context, view: RunsheetView) {
        this.mView = view
        compositeSubscription = CompositeSubscription()
    }

    override fun onDetach() {
        if (mView == null)
            return
        mView = null
        compositeSubscription?.unsubscribe()
        compositeSubscription = null
    }

    override fun sendIntent(intent: Intent) {
        runId = intent.getIntExtra(run_id, -1)
        getInwardRunDetails()
    }

    private fun getInwardRunDetails() {
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

                    inwardRun?.inwardRunItems?.let { items -> inwardRunItems.addAll(items) }

                    groupItems(list)
                }, {
                    mView?.onFailure(it)
                })
        )
    }


    override fun getRunsheetUrl() {
        mView?.onUrlFetched(inwardRun?.runsheetUrl ?: "")
    }

    private fun groupItems(list: List<MpsShipmentCountDTO>) {
        inwardRunDisplayItems.clear()
        inwardRunDisplayItems.addAll(inwardRunItems)

        val mpsShipments =
            inwardRunItems.filter { it.multipartShipment && it.mpsParentId != null }

        inwardRunDisplayItems.removeAll(mpsShipments)

        if (mpsShipments.isNotEmpty()) {
            val groupedShipments = mpsShipments.groupBy { it.mpsParentId }
            for (entry in groupedShipments.entries) {
                val displayShipment = inwardRunItems.first { it.mpsParentId == entry.key }
                /*val shipmentCount =
                    list.find { it.parentShipmentId == displayShipment.mpsParentId.toString() }?.childShipmentCount*/
                displayShipment.mpsCount = entry.value.size
                displayShipment.mpsScannedCount = groupedShipments[entry.key]?.size ?: 0
                inwardRunDisplayItems.add(displayShipment)
            }
        }
        mView?.onInwardRunFetched(
            inwardRun?.createdOn,
            inwardRun?.partnerName,
            inwardRun?.inwardRunItems?.size ?: 0, inwardRunDisplayItems
        )
    }

    override fun getMpsRunItems(position: Int) {
        val parentId = inwardRunDisplayItems[position].mpsParentId
        mView?.onMpsRunItemsFetched(inwardRunDisplayItems[position].mpsCount, inwardRunItems.filter { it.mpsParentId == parentId })
    }
}