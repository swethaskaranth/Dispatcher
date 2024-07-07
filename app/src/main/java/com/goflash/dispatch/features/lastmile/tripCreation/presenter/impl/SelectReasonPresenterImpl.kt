package com.goflash.dispatch.features.lastmile.tripCreation.presenter.impl

import android.content.Context
import com.goflash.dispatch.di.interactor.SortationApiInteractor
import com.goflash.dispatch.features.lastmile.tripCreation.presenter.CancelPresenter
import com.goflash.dispatch.features.lastmile.tripCreation.presenter.SelectReasonPresenter
import com.goflash.dispatch.features.lastmile.tripCreation.view.CancelView
import com.goflash.dispatch.features.lastmile.tripCreation.view.ReasonView
import com.goflash.dispatch.model.ActiveTrips
import com.goflash.dispatch.model.DeleteShipment
import com.goflash.dispatch.model.ShipmentDTO
import com.goflash.dispatch.model.UpdatePincode
import com.goflash.dispatch.util.getTimestampString2
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription

class SelectReasonPresenterImpl(private val sortationApiInteractor: SortationApiInteractor) : SelectReasonPresenter {

    private var view: ReasonView? = null

    private var compositeSubscription: CompositeSubscription? = null

    override fun onAttachView(context: Context, view: ReasonView) {
        this.view = view
        compositeSubscription = CompositeSubscription()
    }

    override fun onDetachView() {
        if (view == null)
            return
        view = null
        compositeSubscription?.unsubscribe()
        compositeSubscription = null
    }

    override fun verifyPincode(pincode: String, shipmentId: String) {
        compositeSubscription?.add(sortationApiInteractor.verifyPincode(pincode, shipmentId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                view?.onSuccess(it)
            }, {
                view?.onFailure(it)
            }))
    }

    override fun cancelShipment(reason: String, shipmentId: String, list: List<ShipmentDTO>) {
        compositeSubscription?.add(sortationApiInteractor.deleteShipment(request(reason, shipmentId,list))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                view?.onSubmitSuccess()
            }, {
                view?.onFailure(it)
            }))
    }

    override fun updatePincode(pincode: String, shipmentId: String, assetId: String, assetName: String) {
        compositeSubscription?.add(sortationApiInteractor.updatePincode(updatePin(pincode, shipmentId, assetId, assetName))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                view?.onSubmitSuccess()
            }, {
                view?.onFailure(it)
            }))
    }

    private fun request(reason: String, shipmentId: String, list: List<ShipmentDTO>): DeleteShipment {
        return DeleteShipment(reason = reason, shipmentId =  shipmentId,childShipment = list)
    }

    private fun updatePin(pincode: String, shipmentId: String, assetId: String, assetName: String): UpdatePincode {
        return UpdatePincode(pincode = pincode, shipmentId =  shipmentId, assetId = assetId , assetName = assetName)
    }

    override fun getChildShipments(shipmentId: String) {
        compositeSubscription?.add(sortationApiInteractor.unassignedShipmentListFilter(ActiveTrips(
            status = "CREATED",
            referenceId = shipmentId,
            excludedTrips = mutableListOf()
        ))
            .map { it ->
                val list = mutableListOf<ShipmentDTO>()
                it.forEach {
                    list.add(
                        ShipmentDTO(shipmentId = it.shipmentId, referenceId = it.referenceId, orderId = it.orderId, assetName = it.assetName, name = it.name, address1 = null,
                            address2 = null, address3 = null, city = null, state = null, pincode = null, contactNumber = null, addressType = null, latitude = null, longitude = null,
                            type = it.type, lbn = it.lbn, committedExpectedDeliveryDate = it.committedExpectedDeliveryDate, priorityType = it.priorityType, packageId = it.packageId,
                            status = it.status, postponedToDate = it.postponedToDate, processingBlocked = it.processingBlocked,shipmentType = it.shipmentType,
                            parentShipment = it.parentShipment,
                            mpsCount = 0)
                    )
                }
                return@map list
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({shipments ->
                view?.onChildShipmentsFetched(shipments.filter { !it.parentShipment.isNullOrEmpty() }.toMutableList())
            }, {
                view?.onFailure(it)
            }))
    }
}