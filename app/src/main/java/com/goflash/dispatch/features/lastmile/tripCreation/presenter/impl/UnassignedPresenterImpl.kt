package com.goflash.dispatch.features.lastmile.tripCreation.presenter.impl

import android.content.Context
import com.goflash.dispatch.data.InTransitTrip
import com.goflash.dispatch.data.UnassignedDTO
import com.goflash.dispatch.di.interactor.SortationApiInteractor
import com.goflash.dispatch.features.lastmile.tripCreation.presenter.UnassignedShipmentPresenter
import com.goflash.dispatch.features.lastmile.tripCreation.view.UnassignedView
import com.goflash.dispatch.model.ActiveTrips
import com.goflash.dispatch.model.ShipmentDTO
import com.goflash.dispatch.type.ShipmentType
import com.goflash.dispatch.util.getTimestampString2
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class UnassignedPresenterImpl(private val sortationApiInteractor: SortationApiInteractor) :
    UnassignedShipmentPresenter {

    private var mView: UnassignedView? = null
    private var compositeSubscription: CompositeSubscription? = null

    private var inTransitTrips: MutableList<InTransitTrip> = mutableListOf()

    override fun onAttach(context: Context, view: UnassignedView) {
        this.mView = view
        compositeSubscription = CompositeSubscription()

        mView?.displayProgress()
        getInTransitTripCount()
    }

    override fun onDetach() {
        if (mView == null)
            return
        mView = null
        compositeSubscription?.unsubscribe()
        compositeSubscription = null
    }

    override fun getShipments(
        type: String?,
        eddStart: String?,
        eddEnd: String?,
        referenceId: String?,
        tag: String?,
        excludedTrips: List<String>,
        serviceType: String?,
        paymentStatus: String?
    ) {

        val mData = if (type == null)
            request(referenceId, excludedTrips)
        else if (eddStart == null || eddEnd == null)
            requestForType(type, tag, excludedTrips)
        else
            request2(type, eddStart, eddEnd, tag, excludedTrips)

        getAllShipments(mData, serviceType,paymentStatus)

    }

    private fun getAllShipments(mData: ActiveTrips, serviceType: String?,paymentStatus: String?) {

        compositeSubscription?.add(
            sortationApiInteractor.unassignedShipmentListFilter(mData)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ res ->

                    val predicates = listOf(
                        { shipment: UnassignedDTO -> (serviceType == null || serviceType == "") || (serviceType.contains(shipment.serviceType)) },
                        { shipment: UnassignedDTO -> (paymentStatus == null || paymentStatus == "both") || shipment.paymentType == paymentStatus }
                                )

                    val shipments = res.filter{ shipment ->
                        predicates.all{it(shipment)}
                        }.toMutableList()

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

                    mView?.onShipmentsFetched(shipments)
                }, {
                    mView?.onFailure(it)
                })
        )

    }

    private fun request(referenceId: String?, excludedTrips: List<String>): ActiveTrips {
        return ActiveTrips(
            status = "CREATED",
            startDate = getTimestampStringFOrMonth(2),
            endDate = getTimestampString2(),
            referenceId = referenceId,
            excludedTrips = excludedTrips
        )
    }

    private fun request2(
        type: String,
        eddStart: String,
        eddEnd: String,
        tag: String?,
        excludedTrips: List<String>
    ): ActiveTrips {
        return ActiveTrips(
            status = "CREATED",
            startDate = getTimestampStringFOrMonth(2),
            endDate = getTimestampString2(),
            type = type,
            eddStartDate = eddStart,
            eddEndDate = eddEnd,
            tag = tag,
            excludedTrips = excludedTrips
        )
    }

    private fun requestForType(
        type: String,
        tag: String?,
        excludedTrips: List<String>
    ): ActiveTrips {
        return ActiveTrips(
            "CREATED",
            type = type,
            startDate = getTimestampStringFOrMonth(2),
            endDate = getTimestampString2(),
            tag = tag,
            excludedTrips = excludedTrips
        )
    }

    private fun getTimestampStringFOrMonth(date: Int): String {
        val calendar = Calendar.getInstance()
        val value = calendar.clone() as Calendar
        value.add(Calendar.MONTH, -2)
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(value.time).replace(" ", "")
    }

    override fun getInTransitTripCount() {
        compositeSubscription?.add(
            sortationApiInteractor.getInTransitTrips()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    inTransitTrips.clear()
                    inTransitTrips.addAll(it)
                    inTransitTrips.map { it.selected = true }
                    mView?.showIntransitCount(inTransitTrips.size)
                }, {
                    mView?.onFailure(it)
                })
        )
    }

    override fun getInTransitTrips(): ArrayList<InTransitTrip> {
        return ArrayList(inTransitTrips)
    }

    override fun setInTransitTrips(list: MutableList<InTransitTrip>) {
        inTransitTrips.clear()
        inTransitTrips.addAll(list)
    }

    override fun getAddressDetails(position: Int, shipmentId: String) {
        compositeSubscription?.add(sortationApiInteractor.viewAddressDetails(shipmentId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                mView?.updateList(position, it)
            }, {
                mView?.onFailure(it)
            })
        )
    }


}