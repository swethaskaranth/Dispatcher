package com.goflash.dispatch.features.lastmile.settlement.presenter.impl

import android.content.Context
import co.uk.rushorm.core.RushSearch
import com.goflash.dispatch.data.FmPickedShipment
import com.goflash.dispatch.data.TripSettlementDTO
import com.goflash.dispatch.di.interactor.SortationApiInteractor
import com.goflash.dispatch.features.lastmile.settlement.presenter.ReceiveFmPickupPresenter
import com.goflash.dispatch.features.lastmile.settlement.view.ReceiveFmPickupView
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription

class ReceiveFmPickupPresenterImpl(private val sortationApiInteractor: SortationApiInteractor) : ReceiveFmPickupPresenter {

    private var mView: ReceiveFmPickupView? = null

    private var tripId : Long? = null

    private var compositeSubscription: CompositeSubscription? = null

    private val shipmentList: MutableList<FmPickedShipment> = mutableListOf()

    private var scannedList: MutableList<FmPickedShipment> = mutableListOf()

    override fun onAttachView(context: Context, view: ReceiveFmPickupView) {
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
        val shipments = RushSearch().whereEqual("tripId",tripId!!).find(FmPickedShipment::class.java)

        shipmentList.clear()
        shipmentList.addAll(shipments)

        scannedList.clear()
        scannedList.addAll(shipmentList.filter { it.isScanned })

        mView?.onShipmentsFetched(scannedList)

        mView?.setShipmentCount(scannedList.size, shipmentList.size)

        if(scannedList.size == shipmentList.size)
            setFlag()

    }

    private fun setFlag(){
        val trip = RushSearch().whereEqual("tripId", tripId!!).findSingle(TripSettlementDTO::class.java)
        if(trip != null){
            trip.isFmPickupScanned = true
            trip.save()
        }
    }

    override fun onBarcodeScanned(barcode: String) {
        if (scannedList.any { it.referenceId == barcode || it.packageId == barcode || it.lbn == barcode }) {
             mView?.onFailure(Throwable("Item already scanned."))
            return
        }
        val shipmentDTO =
            shipmentList.find { it.referenceId == barcode || it.packageId == barcode || it.lbn == barcode }
        if (shipmentDTO != null) {

            if(shipmentDTO.isScanned){
                mView?.showAlreadyScanned(barcode)
                return
            }

            shipmentDTO.isScanned = true
            shipmentDTO.reason = null
            shipmentDTO.save()

            scannedList.add(shipmentDTO)
            mView?.onShipmentsFetched(scannedList)
            mView?.setShipmentCount(scannedList.size, shipmentList.size)
        }else{
            compositeSubscription?.add(sortationApiInteractor.getFmPickupShipment(barcode,tripId!!)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({shipment ->
                    shipment.tripId = tripId
                    shipment.isScanned = true
                    shipment.reason = null
                    shipment.save()

                    shipmentList.add(shipment)
                    scannedList.add(shipment)

                    mView?.onShipmentsFetched(scannedList)
                    mView?.setShipmentCount(scannedList.size, shipmentList.size)

                },{
                    mView?.onFailure(it)
                })
            )
        }
    }
}