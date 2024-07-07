package com.goflash.dispatch.features.lastmile.tripCreation.presenter.impl

import android.content.Context
import com.goflash.dispatch.di.interactor.SortationApiInteractor
import com.goflash.dispatch.features.lastmile.tripCreation.presenter.ScanShipmentPresenter
import com.goflash.dispatch.features.lastmile.tripCreation.view.ScanShipmentView
import com.goflash.dispatch.model.CommonRequest
import com.goflash.dispatch.model.DeleteTrip
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription

class ScanShipmentPresenterImpl (private val sortationApiInteractor: SortationApiInteractor) : ScanShipmentPresenter {

    private var mView: ScanShipmentView? = null
    private var compositeSubscription: CompositeSubscription? = null

    override fun onAttach(context: Context, view: ScanShipmentView) {
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

    override fun getShipmentsForTrip(tripId : Long) {
        compositeSubscription?.add(sortationApiInteractor.getShipmentsforTrip(tripId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({list ->
                mView?.onShipmentsFetched(list)
            },{error ->
                mView?.onFailure(error)

            }))
    }

    override fun addShipment(barcode: String,tripId : Long) {
        compositeSubscription?.add(sortationApiInteractor.addShipmentToTrip(mutableListOf(CommonRequest(tripId,barcode)))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({response ->
                mView?.onAddRemoveSuccess(true)
            },{error ->
                mView?.onFailure(error)

            }))
    }

    override fun removeShipment(barcode: String,tripId : Long) {
        compositeSubscription?.add(sortationApiInteractor.removeShipment(CommonRequest(tripId,barcode))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({response ->
                mView?.onAddRemoveSuccess(false)
            },{error ->
                mView?.onFailure(error)

            }))
    }

    override fun deleteTrip(tripId: Long) {
        compositeSubscription?.add(sortationApiInteractor.deleteTrip(DeleteTrip(tripId,"DELETE"))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({response ->
                mView?.onTripDeleteSuccess()
            },{error ->
                mView?.onFailure(error)

            }))
    }

    override fun getAddressDetails(position: Int, shipmentId: String) {
        compositeSubscription?.add(sortationApiInteractor.viewAddressDetails(shipmentId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                mView?.updateList(position, it)
            },{
                mView?.onFailure(it)
            }))
    }
}