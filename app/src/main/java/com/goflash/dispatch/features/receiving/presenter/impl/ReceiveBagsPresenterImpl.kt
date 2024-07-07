package com.goflash.dispatch.features.receiving.presenter.impl

import android.content.Context
import android.content.Intent
import co.uk.rushorm.core.RushSearch
import com.goflash.dispatch.data.ReceivingDto
import com.goflash.dispatch.data.VehicleDetails
import com.goflash.dispatch.di.interactor.SortationApiInteractor
import com.goflash.dispatch.features.receiving.presenter.ReceiveBagsPresenter
import com.goflash.dispatch.features.receiving.view.ReceiveBagsView
import com.goflash.dispatch.model.BagDetails
import com.goflash.dispatch.util.TRIPID
import com.goflash.dispatch.util.VEHICLEID
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription

/**
 *Created by Ravi on 2019-09-08.
 */
class ReceiveBagsPresenterImpl(val sortationApiInteractor: SortationApiInteractor) :
    ReceiveBagsPresenter {

    private var view: ReceiveBagsView? = null
    private var compositeSubscription: CompositeSubscription? = null

    private var receivingDetails: ReceivingDto? = null
    private var vehicleDetails: MutableList<VehicleDetails>? = null

    private var vehicleId: String? = null
    private var tripId: String? = null
    private var bagId: String? = null
    private var bagIds = hashSetOf<String>()
    private var bags = hashSetOf<String>()
    private var bagDetailsList = mutableListOf<BagDetails>()

    override fun onAttachView(context: Context, view: ReceiveBagsView) {
        this.view = view
        compositeSubscription = CompositeSubscription()
    }

    override fun onDetachView() {
        if (view != null)
            view = null

        compositeSubscription!!.unsubscribe()
        compositeSubscription = null
    }

    override fun onIntent(intent: Intent) {
        vehicleId = intent.getStringExtra(VEHICLEID)
        tripId = intent.getStringExtra(TRIPID)
    }

    override fun onBagScanned(barcode: String) {

        if (bags.contains(barcode)) {
            view?.showMessage("Bag id already scanned.")
            return
        }

        if (!bagIds.contains(barcode)) {
            view?.showMessage("Wrong bag id scanned.")
            return
        }


        if (bagIds.contains(barcode)) {
            vehicleDetails?.forEach {
                if (it.bagId == barcode) {
                    it.isScanned = true
                    it.save()
                    view?.setBagId(barcode, it.destinationName)
                }
            }
        }

        onTaskResume()
    }

    override fun onTaskResume() {
        receivingDetails =
            if (vehicleId != null)
                RushSearch().whereEqual(VEHICLEID, vehicleId).findSingle(ReceivingDto::class.java)
            else
                RushSearch().whereEqual(TRIPID, tripId).findSingle(ReceivingDto::class.java)
        vehicleDetails = if (vehicleId != null)
            RushSearch().whereEqual(VEHICLEID, vehicleId).find(VehicleDetails::class.java)
        else
            RushSearch().whereEqual(TRIPID, tripId).find(VehicleDetails::class.java)
        if (vehicleDetails != null && vehicleDetails!!.isNotEmpty())
            {
            bagDetailsList = RushSearch().find(BagDetails::class.java)

            bagIds = vehicleDetails!!.map { it.bagId }.toHashSet()
            bags = vehicleDetails!!.filter { it.isScanned }.map { it.bagId }.toHashSet()

            view?.onSetViews(receivingDetails!!, vehicleDetails!!, bagDetailsList)

        }else
            getVehicleDetail()

    }

    private fun getVehicleDetail() {
        compositeSubscription?.add(sortationApiInteractor.verifiyVehicleSealScan(null, tripId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ list ->
                list.forEach {
                    it.vehicleId = vehicleId
                    it.tripId = tripId
                    it.save()
                }

                val receivingDto =
                    RushSearch().whereEqual(TRIPID, tripId).findSingle(ReceivingDto::class.java)
                receivingDto?.let {
                    it.status = "PROCESSED"
                    it.save()
                }

                onTaskResume()

            }, { error ->
                view?.onFailure(error)
            })
        )
    }

}