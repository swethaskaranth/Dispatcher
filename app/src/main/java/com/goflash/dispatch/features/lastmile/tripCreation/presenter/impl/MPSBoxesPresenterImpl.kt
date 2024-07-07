package com.goflash.dispatch.features.lastmile.tripCreation.presenter.impl

import android.content.Context
import com.goflash.dispatch.app_constants.reference_id
import com.goflash.dispatch.data.ChildShipmentDTO
import com.goflash.dispatch.di.interactor.SortationApiInteractor
import com.goflash.dispatch.features.lastmile.tripCreation.presenter.MPSBoxesPresenter
import com.goflash.dispatch.features.lastmile.tripCreation.view.MPSBoxesView
import com.goflash.dispatch.model.ActiveTrips
import com.goflash.dispatch.model.ShipmentDTO
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription

class MPSBoxesPresenterImpl(private val sortationApiInteractor: SortationApiInteractor) :
    MPSBoxesPresenter {

    private var mView: MPSBoxesView? = null
    private var compositeSubscription: CompositeSubscription? = null

    override fun onAttach(context: Context, view: MPSBoxesView) {
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

    override fun fetchChildShipments(parentShipment: String) {
        compositeSubscription?.add(sortationApiInteractor.unassignedShipmentListFilter(
            ActiveTrips(
                status = "CREATED",
                referenceId = parentShipment,
                excludedTrips = mutableListOf()
            )
        )
            .map { it ->
                val list = mutableListOf<ChildShipmentDTO>()
                it.forEach {
                    list.add(
                        ChildShipmentDTO(
                            shipmentId = it.shipmentId,
                            lbn = it.lbn,
                            referenceId = it.referenceId,
                            packageId = it.packageId,
                            parentShipment = "",
                            status = it.status
                        )

                    )
                }
                return@map list
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ shipments ->
                mView?.onChildShipmentsFetched(shipments.filter { it.referenceId != parentShipment }
                    .toMutableList())
            }, {
                mView?.onFailure(it)
            })
        )
    }
}