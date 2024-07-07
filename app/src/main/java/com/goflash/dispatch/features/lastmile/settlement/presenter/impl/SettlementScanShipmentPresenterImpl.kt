package com.goflash.dispatch.features.lastmile.settlement.presenter.impl

import android.content.Context
import co.uk.rushorm.core.RushSearch
import com.goflash.dispatch.data.*
import com.goflash.dispatch.di.interactor.SortationApiInteractor
import com.goflash.dispatch.features.lastmile.settlement.presenter.SettlementScanShipmentPresenter
import com.goflash.dispatch.features.lastmile.settlement.view.SettlementScanShipmentView
import com.goflash.dispatch.model.DispatchShipmentRequest
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription

class SettlementScanShipmentPresenterImpl(private val sortationApiInteractor: SortationApiInteractor) :
    SettlementScanShipmentPresenter {

    private var mView: SettlementScanShipmentView? = null

    private var compositeSubscription: CompositeSubscription? = null

    private val shipmentList: MutableList<ReturnShipmentDTO> = mutableListOf()

    private var tripId : Long? = null

    override fun onAttachView(context: Context, view: SettlementScanShipmentView) {
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

    override fun setTripId(tripId: Long) {
        this.tripId = tripId
    }

    override fun getShipments() {

        val trip = RushSearch().whereEqual("tripId", tripId!!).findSingle(TripSettlementDTO::class.java)

        val shipments = RushSearch().whereChildOf(TripSettlementDTO::class.java,"returnShipment",trip!!.id).find(ReturnShipmentDTO::class.java)

        shipmentList.clear()
        shipmentList.addAll(shipments)

        mView?.onShipmentsFetched(shipmentList.filter { it.isScanned }.map { it.referenceId })

        val scanned = shipmentList.filter { it.isScanned }.size
        mView?.setShipmentCount(scanned, shipmentList.size)

        if(scanned == shipmentList.size)
            setFlag()

        mView?.enableOrDisableProceed(scanned == shipmentList.size)

    }

    private fun setFlag(){
        val trip = RushSearch().whereEqual("tripId", tripId!!).findSingle(TripSettlementDTO::class.java)
        if(trip != null){
            trip.isReturnScanned = true
            trip.save()
        }
    }

    override fun onBarcodeScanned(barcode: String, tripId: Long) {

        val shipmentDTOList =
            shipmentList.filter { it.referenceId == barcode || it.packageId == barcode || it.lbn == barcode }

        if (shipmentDTOList.isNotEmpty()) {

            if (shipmentDTOList.all { it.isScanned }) {
                mView?.onFailure(Throwable("Shipment already scanned."))
                return
            }

            val shipmentDTO = shipmentDTOList.find { !it.isScanned }

            val items =
                RushSearch().whereEqual("shipmentId", shipmentDTO!!.shipmentId).find(Item::class.java)
            if (items == null || items.isEmpty())
                compositeSubscription?.add(
                    sortationApiInteractor.getReturnedItens(
                        tripId,
                        DispatchShipmentRequest(shipmentDTO.shipmentId)
                    )
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ itemList ->

                            for (item in itemList) {
                                item.shipmentId = shipmentDTO.shipmentId
                            }
                            val returnShipment = RushSearch().whereEqual("shipmentId",shipmentDTO.shipmentId).findSingle(ReturnShipmentDTO::class.java)
                            returnShipment.items = itemList
                            returnShipment.save()

                            if (itemList.any { it.barcode != null && it.barcode.isNotEmpty() })
                                mView?.takeToScanActivity(
                                    shipmentDTO.shipmentId,
                                    shipmentDTO.referenceId,
                                    shipmentDTO.isPartialDelivery
                                )
                            else
                                mView?.takeToItemSummaryActivity(
                                shipmentDTO.shipmentId,
                                shipmentDTO.referenceId,
                                shipmentDTO.isPartialDelivery
                            )

                        }, { error ->
                            mView?.onFailure(error)
                        })
                )
            else {
                if (items.any { it.barcode != null && it.barcode.isNotEmpty() })
                    mView?.takeToScanActivity(
                        shipmentDTO.shipmentId,
                        shipmentDTO.referenceId,
                        shipmentDTO.isPartialDelivery
                    )
                else
                    mView?.takeToItemSummaryActivity(
                        shipmentDTO.shipmentId,
                        shipmentDTO.referenceId,
                        shipmentDTO.isPartialDelivery
                    )
            }

        } else
            mView?.onFailure(Throwable("Invalid barcode. Please scan valid barcode"))

    }

    override fun onNext(tripId: Long) {
        val fmShipments = RushSearch().whereEqual("tripId", tripId).find(FmPickedShipment::class.java)
        if(fmShipments.isNullOrEmpty()) {
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
        else
            mView?.startReceiveFmPickupActivity()
    }
}