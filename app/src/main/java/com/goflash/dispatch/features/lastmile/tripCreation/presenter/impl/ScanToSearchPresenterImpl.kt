package com.goflash.dispatch.features.lastmile.tripCreation.presenter.impl

import android.content.Context
import com.goflash.dispatch.di.interactor.SortationApiInteractor
import com.goflash.dispatch.features.lastmile.tripCreation.presenter.ScanToSearchPresenter
import com.goflash.dispatch.features.lastmile.tripCreation.view.ScanToSearchView
import com.goflash.dispatch.model.ActiveTrips
import com.goflash.dispatch.model.ShipmentDTO
import com.goflash.dispatch.type.ShipmentType
import com.goflash.dispatch.util.PreferenceHelper
import com.goflash.dispatch.util.getTimestampString2
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription
import java.text.SimpleDateFormat
import java.util.*

class ScanToSearchPresenterImpl(private val sortationApiInteractor: SortationApiInteractor) :
    ScanToSearchPresenter {


    private var mView : ScanToSearchView? = null
    private var compositeSubscription : CompositeSubscription? = null

    override fun onAttach(context: Context, view: ScanToSearchView) {
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

    override fun onBarcodeScanned(barcode: String) {

        searchShipment(request(barcode))
    }

    private fun searchShipment(activeTrips: ActiveTrips){
        compositeSubscription?.add(sortationApiInteractor.unassignedShipmentListFilter(activeTrips)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({res ->
                val shipments = res.toMutableList()
                val mpsShipments =
                    shipments.filter { it.shipmentType == ShipmentType.MPS.name && !it.parentShipment.isNullOrEmpty() }
                shipments.removeAll(mpsShipments)

                if(mpsShipments.isNotEmpty()){
                    val groupedShipments = mpsShipments.groupBy { it.parentShipment }
                    for (entry in groupedShipments.entries) {
                        val parentShipment = shipments.first { it.shipmentId == entry.key }
                        parentShipment.mpsCount = groupedShipments[entry.key]?.size?:0
                    }
                }
                mView?.onSuccess(shipments)
            }, {
                mView?.onFailure(it)
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

    private fun request(id: String): ActiveTrips{
        return ActiveTrips(
            status = "CREATED",
            referenceId = id,
            startDate = getTimestampStringFOrMonth(),
            endDate = getTimestampString2()
            ,excludedTrips = mutableListOf()
        )
    }

    private fun getTimestampStringFOrMonth(): String {
        val calendar = Calendar.getInstance()
        val value = calendar.clone() as Calendar
        value.add(Calendar.MONTH, -2)
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(value.time).replace(" ", "")
    }
}