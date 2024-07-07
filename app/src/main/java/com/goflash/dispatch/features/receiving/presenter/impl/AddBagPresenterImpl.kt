package com.goflash.dispatch.features.receiving.presenter.impl

import android.content.Context
import android.content.Intent
import co.uk.rushorm.core.RushSearch
import com.goflash.dispatch.data.BagDTO
import com.goflash.dispatch.data.VehicleDetails
import com.goflash.dispatch.di.interactor.SortationApiInteractor
import com.goflash.dispatch.features.receiving.presenter.AddBagPresenter
import com.goflash.dispatch.features.receiving.view.AddBagView
import com.goflash.dispatch.model.BagDetails
import com.goflash.dispatch.model.CommonRequest
import com.goflash.dispatch.util.BAGID
import com.goflash.dispatch.util.RETURNREASON
import com.goflash.dispatch.util.TRIPID
import com.goflash.dispatch.util.VEHICLEID
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription

/**
 *Created by Ravi on 2019-09-08.
 */
class AddBagPresenterImpl(val sortationApiInteractor: SortationApiInteractor): AddBagPresenter {

    private var view: AddBagView? = null

    private var compositeSubscription: CompositeSubscription? = null

    private var items = hashSetOf<String>()
    private var bagList = mutableListOf<VehicleDetails>()
    private var vehicleId: String? = null
    private var tripId: String? = null

    override fun onAttachView(context: Context, view: AddBagView) {
        this.view = view
        compositeSubscription = CompositeSubscription()
    }

    override fun onDetachView() {
        if(view != null)
            view = null

        compositeSubscription!!.unsubscribe()
        compositeSubscription = null
    }

    override fun onBagScanned(barcode: String) {
        val bag = if(vehicleId != null)
            RushSearch().whereEqual(BAGID, barcode)//.and().whereEqual(RETURNREASON,"added")
            .and().whereEqual(VEHICLEID, vehicleId).findSingle(VehicleDetails::class.java)
        else
            RushSearch().whereEqual(BAGID, barcode)//.and().whereEqual(RETURNREASON,"added")
                .and().whereEqual(TRIPID, tripId).findSingle(VehicleDetails::class.java)

        if(bag != null){
            view?.showMessage("BagId already scanned")
            return
        }

        getBagDetail(barcode)
    }

    private fun getBagDetail(bagId : String){
        view?.onShowProgress()
        compositeSubscription?.add(sortationApiInteractor.getBagDetailsToAddToTrip(bagId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({bag -> onAddBag(bag)
            },{error ->
                view?.onFailure(error)
            }))
    }

    private fun onAddBag(bag: BagDTO){

        val vDetails = VehicleDetails()
        vDetails.bagId = bag.bagId
        vDetails.returnReason = "added"
        vDetails.vehicleId = vehicleId
        vDetails.tripId = tripId
        vDetails.isScanned = true
        vDetails.save()

        items.add(bag.bagId)
        view?.onSuccess(items)
    }

    override fun onTaskResume() {
        bagList.clear()
        items.clear()

        bagList = if(vehicleId != null)
            RushSearch().whereEqual(VEHICLEID,vehicleId).and().whereEqual(RETURNREASON, "added").find(VehicleDetails::class.java)
        else
            RushSearch().whereEqual(TRIPID,tripId).and().whereEqual(RETURNREASON, "added").find(VehicleDetails::class.java)

        items.addAll(bagList.map { it.bagId })
        view?.updateBag(items.size)
    }

    override fun onIntent(intent: Intent) {
        vehicleId = intent.getStringExtra(VEHICLEID)
        tripId = intent.getStringExtra(TRIPID)
    }
}