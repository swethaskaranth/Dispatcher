package com.goflash.dispatch.features.lastmile.tripCreation.presenter.impl

import android.content.Context
import com.goflash.dispatch.app_constants.TASKTYPE
import com.goflash.dispatch.data.InTransitTrip
import com.goflash.dispatch.data.UnassignedDTO
import com.goflash.dispatch.di.interactor.SortationApiInteractor
import com.goflash.dispatch.features.lastmile.tripCreation.presenter.AddShipmentPresenter
import com.goflash.dispatch.features.lastmile.tripCreation.view.AddShipmentView
import com.goflash.dispatch.model.ActiveTrips
import com.goflash.dispatch.model.CommonRequest
import com.goflash.dispatch.type.ShipmentType
import com.goflash.dispatch.type.TaskStatus
import com.goflash.dispatch.util.PreferenceHelper
import com.goflash.dispatch.util.getTimestampString
import com.goflash.dispatch.util.getTimestampString2
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription
import java.text.SimpleDateFormat
import java.util.*


class AddShipmentPresenterImpl(private val sortationApiInteractor: SortationApiInteractor) :
    AddShipmentPresenter {

    private var mView: AddShipmentView? = null
    private var compositeSubscription: CompositeSubscription? = null

    private var start: String? = null
    private var end: String? = null

    private var inTransitTrips: MutableList<InTransitTrip> = mutableListOf()

    override fun onAttach(context: Context, view: AddShipmentView) {
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
        tag: String?,
        excludedTrips: List<String>,
        serviceType: String?,
        paymentStatus: String?
    ) {

        val mData = if (type == null)
            request(excludedTrips)
        else if (eddStart == null || eddEnd == null)
            requestForPickup(type, tag, excludedTrips)
        else
            request2(type, eddStart, eddEnd, tag, excludedTrips)

        compositeSubscription?.add(
            sortationApiInteractor.unassignedShipmentListFilter(mData)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ shipments ->
                    /*if (serviceType != null && serviceType != "both") {
                        groupShipments(shipments.filter { it.consumerType == serviceType }
                            .toMutableList())
                    } else
                        groupShipments(shipments.toMutableList())*/

                    val predicates = listOf(
                        { shipment: UnassignedDTO -> (serviceType == null || serviceType == "") || (serviceType.contains(shipment.serviceType)) },
                        { shipment: UnassignedDTO -> (paymentStatus == null || paymentStatus == "both") || shipment.paymentType == paymentStatus }
                                )

                    val filteredShipments = shipments.filter{ shipment ->
                        predicates.all{it(shipment)}
                        }.toMutableList()

                    groupShipments(filteredShipments)

                }, { error ->
                    mView?.onFailure(error)

                })
        )
    }

    /*   private fun getFromAndToDates() {
           var cal1 = Calendar.getInstance()
           *//*cal1.set(Calendar.HOUR_OF_DAY, 0)
        cal1.set(Calendar.MINUTE, 0)
        cal1.set(Calendar.SECOND, 0)*//*

        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
        end = dateFormat.format(cal1.time)

        cal1 = Calendar.getInstance()
        cal1.set(Calendar.HOUR_OF_DAY, 0)
        cal1.set(Calendar.MINUTE, 0)
        cal1.set(Calendar.SECOND, 0)

        cal1.add(Calendar.DATE, -7)
        start = dateFormat.format(cal1.time)

    }*/

    private fun groupShipments(shipments: MutableList<UnassignedDTO>) {

        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")

        val mpsShipments =
            shipments.filter { it.shipmentType == ShipmentType.MPS.name && !it.parentShipment.isNullOrEmpty() }
        shipments.removeAll(mpsShipments)

        if (mpsShipments.isNotEmpty()) {
            val groupedShipments = mpsShipments.groupBy { it.parentShipment }
            for (entry in groupedShipments.entries) {
                val parentShipment = shipments.first { it.shipmentId == entry.key }
                parentShipment.mpsCount = groupedShipments[entry.key]?.size ?: 0
            }
        }


        val pickupShipments = shipments.filter { it.type == ShipmentType.RETURN.name || it.type == ShipmentType.FMPICKUP.name  }
        val deliveryShipments = shipments.filter { it.type == ShipmentType.FORWARD.name }

        val groupPickupByPincode =
            pickupShipments.filter {
                it.status != TaskStatus.BLOCKED.name && (it.postponedToDate == null || Date().after(
                    format.parse(it.postponedToDate)
                ) || !it.processingBlocked)
            }
                .groupBy { it.pickupPincode }

        val groupDeliveryByPincode = deliveryShipments.filter {
            it.status != TaskStatus.BLOCKED.name && (it.postponedToDate == null || Date().after(
                format.parse(it.postponedToDate)
            ) || !it.processingBlocked)
        }
            .groupBy { it.dropPincode }

        val groupByPincode = mutableMapOf<String, MutableList<UnassignedDTO>>()

        for (entry in groupDeliveryByPincode.entries) {
            groupByPincode[entry.key] = entry.value.toMutableList()
        }

        for (entry in groupPickupByPincode.entries) {
            val pickupPincode = groupByPincode[entry.key]
            if (pickupPincode == null)
                groupByPincode[entry.key] = entry.value.toMutableList()
            else
                pickupPincode.addAll(entry.value)

        }
        mView?.onShipmentsFetched(groupByPincode.toSortedMap())
    }

    override fun addShipments(shipments: MutableList<CommonRequest>) {
        compositeSubscription?.add(
            sortationApiInteractor.addShipmentToTrip(shipments)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    mView?.onAddSuccess()
                }, { error ->
                    mView?.onFailure(error)

                })
        )
    }

    private fun request(excludedTrips: List<String>): ActiveTrips {
        return ActiveTrips(
            "CREATED",
            getTimestampStringFOrMonth(2),
            getTimestampString2(),
            excludedTrips = excludedTrips
        )
    }

    private fun requestForPickup(
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

    private fun getTimestampStringFOrMonth(date: Int): String {
        val calendar = Calendar.getInstance()
        val value = calendar.clone() as Calendar
        value.add(Calendar.MONTH, -2)
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(value.time).replace(" ", "")
    }

    override fun getInTransitTripCount() {
        compositeSubscription?.add(sortationApiInteractor.getInTransitTrips()
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

    override fun getInTransitTrips(): ArrayList<InTransitTrip> {
        return ArrayList(inTransitTrips)
    }

    override fun setInTransitTrips(list: MutableList<InTransitTrip>) {
        inTransitTrips.clear()
        inTransitTrips.addAll(list)
    }

}