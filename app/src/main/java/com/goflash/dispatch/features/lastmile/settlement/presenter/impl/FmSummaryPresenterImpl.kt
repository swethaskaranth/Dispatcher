package com.goflash.dispatch.features.lastmile.settlement.presenter.impl

import android.content.Context
import co.uk.rushorm.core.RushSearch
import com.goflash.dispatch.data.AckSlipDto
import com.goflash.dispatch.data.FmPickedShipment
import com.goflash.dispatch.data.PoaResponseForRecon
import com.goflash.dispatch.data.TripSettlementDTO
import com.goflash.dispatch.di.interactor.SortationApiInteractor
import com.goflash.dispatch.features.lastmile.settlement.presenter.FmSummaryPresenter
import com.goflash.dispatch.features.lastmile.settlement.view.FmSummaryView
import rx.subscriptions.CompositeSubscription

class FmSummaryPresenterImpl(private val sortationApiInteractor: SortationApiInteractor) : FmSummaryPresenter {

    private var mView: FmSummaryView? = null

    private var compositeSubscription: CompositeSubscription? = null

    private val groupedShipments: MutableMap<String,List<FmPickedShipment>> = mutableMapOf()

    override fun onAttachView(context: Context, view: FmSummaryView) {
        this.mView = view
        compositeSubscription = CompositeSubscription()
    }

    override fun onDetachView() {
        if (mView == null)
            return
        mView = null
        compositeSubscription?.unsubscribe()
        compositeSubscription = null
    }

    override fun getShipments(tripId: Long) {
        val shipments = RushSearch().whereEqual("tripId", tripId).find(FmPickedShipment::class.java)
        groupedShipments.clear()
        groupedShipments.putAll(shipments.groupBy { it.originName })
        mView?.onShipmentsFetched(groupedShipments)
        mView?.enableOrDisableProceed(groupedShipments.any {map -> map.value.filter { it.isScanned }.size.plus(map.value.filter { it.reason != null }.size) == map.value.size })
    }

    override fun onNext(tripId: Long) {
        val ackSlips = RushSearch().whereEqual("tripId", tripId).find(AckSlipDto::class.java)
        if(ackSlips.isNullOrEmpty()) {
            val trip =
                RushSearch().whereEqual("tripId", tripId).findSingle(TripSettlementDTO::class.java)
            val deliverySlips = RushSearch().whereChildOf(
                TripSettlementDTO::class.java, "poas",
                trip!!.id
            )
                .find(PoaResponseForRecon::class.java)
            if(deliverySlips.isNullOrEmpty())
            mView?.startStep3CashActivity()
            else
                mView?.startAckDeliverySlipReconActivity()
        }
        else
            mView?.startVerifyImageActivity()

    }

}