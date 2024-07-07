package com.goflash.dispatch.features.lastmile.settlement.presenter.impl

import android.content.Context
import co.uk.rushorm.core.RushCore
import co.uk.rushorm.core.RushSearch
import com.goflash.dispatch.data.*
import com.goflash.dispatch.di.interactor.SortationApiInteractor
import com.goflash.dispatch.features.lastmile.settlement.presenter.Step3Presenter
import com.goflash.dispatch.features.lastmile.settlement.view.Step3View
import com.goflash.dispatch.type.AckSource
import com.goflash.dispatch.type.AckStatus
import com.goflash.dispatch.type.PoaType
import com.goflash.dispatch.type.ShipmentType
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription

class Step3PresenterImpl(private val sortationApiInteractor: SortationApiInteractor) : Step3Presenter {


    private var mView : Step3View? = null
    private var compositeSubscription : CompositeSubscription? = null

    override fun onAttach(context: Context, view: Step3View) {
        this.mView = view
        compositeSubscription = CompositeSubscription()
    }

    override fun onDetach() {
        if(mView == null)
            return
        mView = null
        compositeSubscription?.unsubscribe()
        compositeSubscription = null
    }


    override fun settleTrip(tripId: String, cashAmount: String, chequeAmount: String) {
        val settleTripData = getCashCollection(tripId)
        val fmShipments = RushSearch().whereEqual("tripId",tripId).find(FmPickedShipment::class.java)
        val fmShipmentList = ArrayList<UndeliveredShipmentDTO>()
        fmShipments.forEach {
            val shipment = UndeliveredShipmentDTO(it.shipmentId, it.lbn, it.referenceId, it.packageId,
                ShipmentType.FMPICKUP.name, if(it.reason != null) "Pending" else "Completed",null, null, null, null,it.reason.isNullOrEmpty(),it.reason,null,null)
            fmShipmentList.add(shipment)
        }
        val undeliveredShipments = settleTripData.undeliveredShipment
        undeliveredShipments.addAll(fmShipmentList)
        settleTripData.undeliveredShipment = undeliveredShipments

        val returnShipments = settleTripData.returnShipment
        for (returnShipment in returnShipments) {
            val reconImages = RushSearch().whereEqual("shipmentId", returnShipment.shipmentId).find(ReconImageDTO::class.java)
            reconImages.map { it.pathString }
            returnShipment.reconImages = ReturnReconImageDTO(reconImages.map { it.pathString })
        }

        //settleTripData.fmPickedupShipments = fmShipments.groupBy { it.originAddressId.toString() }
        val ackSlips = RushSearch().whereEqual("tripId",tripId).find(AckSlipDto::class.java)
        ackSlips.forEach {
            if(it.status == null)
                it.status = AckStatus.REJECTED.name
            if(it.source != AckSource.DISPATCHER.name)
                it.url = null
            it.tripId = null
        }
        settleTripData.ackSlips = ackSlips

        val trip =
            RushSearch().whereEqual("tripId", tripId).findSingle(TripSettlementDTO::class.java)
        val deliverySlips = RushSearch().whereChildOf(
            TripSettlementDTO::class.java, "poas",
            trip!!.id
        )
            .find(PoaResponseForRecon::class.java)
        val poaSatisfied: MutableMap<Long, List<PoaSatisfiedDTO>> = mutableMapOf()
        deliverySlips.forEach {
            poaSatisfied[it.taskId] = mutableListOf(PoaSatisfiedDTO(PoaType.VERIFY_CASH_RECEIPT, true))
        }

        settleTripData.poaSatisfied = poaSatisfied

        settleTripData.cashAmountReceived = cashAmount.toLongOrNull() ?: 0L
        settleTripData.chequeAmountReceived= chequeAmount.toLongOrNull() ?: 0L


        compositeSubscription?.add(sortationApiInteractor.settleTrip(tripId = tripId.toLong(), tripSettlementDTO = settleTripData)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                RushCore.getInstance().clearDatabase()
                var successMessage = it.remarks;
                if(it.unassignedTripDetails != null) {
                    successMessage += "\n Removed assigned sprinter from trips ${it.unassignedTripDetails.tripId} as sprinter outstanding amount breached."
                }
                mView?.onSuccess(successMessage)
            }, {
                mView?.onFailure(it)
            }))
    }

    override fun getCashCollection(tripId: String): TripSettlementCompleteDTO {
        return TripSettlementCompleteDTO(RushSearch().whereEqual("tripId", tripId.toLong()).findSingle(TripSettlementDTO::class.java))

    }
}