package com.goflash.dispatch.features.rtoReceiving.presenter.impl

import android.content.Context
import android.content.Intent
import co.uk.rushorm.core.RushSearch
import com.goflash.dispatch.app_constants.run_id
import com.goflash.dispatch.data.InwardRunItem
import com.goflash.dispatch.data.ReceivingChildShipmentDTO
import com.goflash.dispatch.data.UpdateInwardRunItemRequest
import com.goflash.dispatch.di.interactor.SortationApiInteractor
import com.goflash.dispatch.features.rtoReceiving.presenter.ReceiveMpsShipmentListPresenter
import com.goflash.dispatch.features.rtoReceiving.view.ReceiveMpsShipmentListView
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription

class ReceiveMpsShipmentListPresenterImpl(val sortationApiInteractor: SortationApiInteractor) :
    ReceiveMpsShipmentListPresenter {

    private var mView: ReceiveMpsShipmentListView? = null
    private var compositeSubscription: CompositeSubscription? = null

    private var runId: Int = 0
    private var runItems: MutableList<InwardRunItem> = mutableListOf()

    private var totalCount: Int = 0

    override fun onAttach(context: Context, view: ReceiveMpsShipmentListView) {
        this.mView = view
        compositeSubscription = CompositeSubscription()
    }

    override fun sendIntent(intent: Intent) {
        runId = intent.getIntExtra(run_id, -1)
        totalCount = intent.getIntExtra("total", 0)
        val items = intent.getParcelableArrayExtra("mpsRunItems")?.map { it as InwardRunItem }
        items?.toMutableList()?.let { runItems.addAll(it) }

        val shipments = RushSearch().whereEqual("parentShipmentId", runItems[0].mpsParentId ?: 0)
            .find(ReceivingChildShipmentDTO::class.java)
        shipments.removeAll { shipment ->
            shipment.lbn == runItems.find { it.lbn == shipment.lbn }?.lbn
        }

        shipments.forEach {
            val runItem = InwardRunItem(-1, it.lbn, false, true, -1,status = it.receivedStatus, referenceId = it.referenceId, wayBillNumber = it.wayBillNumber)
            runItems.add(runItem)
        }

        mView?.setupData(totalCount, runItems)
    }

    override fun onDetach() {
        if (mView == null)
            return
        mView = null
        compositeSubscription?.unsubscribe()
        compositeSubscription = null
    }

    override fun getExceptionReasons(position: Int) {
        val runItem = runItems[position]
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
            runItems.find { it.wayBillNumber == wayBillNumber || it.lbn == wayBillNumber }
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

                }, {
                    mView?.onFailure(it)
                })
        )
    }
}