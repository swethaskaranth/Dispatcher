package com.goflash.dispatch.features.lastmile.tripCreation.presenter.impl

import android.content.Context
import co.uk.rushorm.core.RushCore
import com.goflash.dispatch.data.InTransitTrip
import com.goflash.dispatch.data.SmartTripResponse
import com.goflash.dispatch.data.SprinterForZone
import com.goflash.dispatch.data.UnassignedDTO
import com.goflash.dispatch.di.interactor.SortationApiInteractor
import com.goflash.dispatch.features.lastmile.tripCreation.presenter.AssignZonePresenter
import com.goflash.dispatch.features.lastmile.tripCreation.view.AssignZoneView
import com.goflash.dispatch.model.*
import com.goflash.dispatch.type.PriorityType
import com.goflash.dispatch.type.TaskStatus
import com.goflash.dispatch.util.getTimestampString2
import com.goflash.dispatch.util.getTimestampStringForMonth
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription
import java.lang.Integer.max
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.ceil

class AssignZonePresenterImpl(private val sortationApiInteractor: SortationApiInteractor) :
    AssignZonePresenter {

    private var mView: AssignZoneView? = null

    private var compositeSubscription: CompositeSubscription? = null

    private var masterList: MutableList<ZoneSprinterDTO> = mutableListOf()
    private var filteredShipments : MutableList<UnassignedDTO> = mutableListOf()

    private var zoneList: MutableList<ZoneSprinterDTO> = mutableListOf()
    private var filteredZoneList: MutableList<ZoneSprinterDTO> = mutableListOf()

    private var inTransitTrips: MutableList<InTransitTrip> = mutableListOf()

    private var zoneIdsOfTrips : MutableList<SmartTripResponse> = mutableListOf()

    private var maxTasksPerSprinterMap : MutableMap<Int, Int> = mutableMapOf()
    private var mergedMaxTasksPerSprinterMap : MutableMap<Int, Int> = mutableMapOf()

    override fun onAttachView(context: Context, view: AssignZoneView) {
        this.mView = view
        compositeSubscription = CompositeSubscription()
        clearSprinters()

        mView?.displayProgress()
        getInTransitTripCount()
    }

    override fun onDetachView() {
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
        excludedTrips: List<String>,
        tag : String?,
        serviceType: String?,
        paymentStatus: String?
    ) {

        val mData = if (type == null)
            request(excludedTrips)
        else
            request2(type, eddStart, eddEnd,tag, excludedTrips)

        filteredShipments.clear()

        compositeSubscription?.add(
            sortationApiInteractor.unassignedShipmentListFilter(mData)
                // Fetch data for validation
                .flatMap { shipments ->
                    val zoneIds = shipments.mapNotNull { it.zoneId }.distinct()
                    sortationApiInteractor.getMaxTasksPerSprinter(zoneIds)
                        .flatMap {
                            maxTasksPerSprinterMap = it.toMutableMap()
                            Observable.just(shipments)
                        }
                }
                // Fetch on-going smart trip details
                .flatMap { shipments ->
                    sortationApiInteractor.smartTripProcess()
                        .flatMap { response ->
                            zoneIdsOfTrips.clear()
                            response?.let {
                                zoneIdsOfTrips.addAll(response)
                            }
                            Observable.just(shipments)
                        }
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ shipments ->

                    val predicates = listOf(
                        { shipment: UnassignedDTO -> (serviceType == null || serviceType == "both") || (serviceType.contains(shipment.serviceType)) },
                        { shipment: UnassignedDTO -> (paymentStatus == null || paymentStatus == "both") || shipment.paymentType == paymentStatus }
                                )

                    filteredShipments.addAll(shipments.filter{ shipment ->
                        predicates.all{it(shipment)}
                        }.toMutableList())

                    groupShipments(filteredShipments)

                }, { error ->
                    mView?.onFailure(error)

                })
        )
    }

    private fun groupShipments(shipments: MutableList<UnassignedDTO>) {

        zoneList.clear()
        masterList.clear()

        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")

        shipments.removeAll { it.zoneName == null }

        val groupByZone =
            shipments.filter {
                it.status != TaskStatus.BLOCKED.name && (it.postponedToDate == null || Date().after(
                    format.parse(it.postponedToDate)
                ) || !it.processingBlocked)
            }
                .groupBy { it.zoneName }

        for (entry in groupByZone.entries) {
            val zone = ZoneListDTO(
                entry.value[0].zoneId,
                entry.key,
                entry.value.size,
                entry.value.filter { it.priorityType == PriorityType.HIGH.name }.size
            )

            val zoneIdOfTrip = zoneIdsOfTrips.find { it.zoneIds.any {i -> i==zone.id  } }
            if(zoneIdOfTrip!= null){
                zone.tripCreationInProgress = true
                zone.tripProcessId = zoneIdOfTrip.id?:0
            }
            zoneList.add(ZoneSprinterDTO(mutableListOf(), mutableListOf(zone.copy())))
            masterList.add(ZoneSprinterDTO(mutableListOf(), mutableListOf(zone)))
        }

        zoneIdsOfTrips.forEach {
            if(it.zoneIds.size > 1){
                val mergedZone : MutableList<ZoneSprinterDTO> = mutableListOf()
                it.zoneIds.forEach { zoneId ->
                    val zone = zoneList.find {z -> z.zoneList.any { zone -> zone.id == zoneId } }
                    mergedZone.add(zone!!)
                }
                mergeZones(mergedZone)
            }
        }

        mView?.onShipmentsFetched(zoneList)
    }

    override fun mergeZones(list: MutableList<ZoneSprinterDTO>) {
        val index = zoneList.indexOf(list[0])
        val mergedZone = zoneList[index]
        mergedZone.zoneList.map { it.selected = false }
        mergedZone.sprinterList.clear()
        for (sprinter in mergedZone.sprinterList)
            sprinter.delete()
        mergedMaxTasksPerSprinterMap[list[0].zoneList[0].id]  = list
            .map { maxTasksPerSprinterMap[it.zoneList[0].id] ?: 0 }
            .reduce { a, b -> max(a,b) }
        for (i in 1 until list.size) {
            val zone = list[i]
            mergedZone.zoneList.addAll(zone.zoneList)

            zone.zoneList.map { it.selected = false }

            for (sprinter in zone.sprinterList)
                sprinter.delete()

            zone.sprinterList.clear()


            zoneList.remove(zone)
        }

        mView?.onShipmentsFetched(zoneList)
    }


    override fun deMergeZone(zoneId: Int) {
        val zone: ZoneSprinterDTO? = zoneList.find { z ->
            z.zoneList.any { it.id == zoneId }
        }
        if (zone != null) {
            zoneList.remove(zone)
            val zones = zone.zoneList
            zones.forEach { z ->
                val masterCopy = masterList.indexOfFirst { it.zoneList.any { it.id == z.id } }
                if(masterCopy != -1){
                    zoneList.add(
                        masterCopy.coerceAtMost(zoneList.size),
                        ZoneSprinterDTO(mutableListOf(), mutableListOf(masterList[masterCopy].zoneList[0]))
                    )
                }
            }
        }
        mergedMaxTasksPerSprinterMap.remove(zoneId)
        mView?.onShipmentsFetched(zoneList)
    }

    override fun onItemSelected(zoneId: Int) {
        if (filteredZoneList.isNotEmpty())
            for (zone in filteredZoneList) {
                val zoneDto = zone.zoneList.find { it.id == zoneId }
                if (zoneDto != null) {
                    val minSprinterCount = getMinSprinterCount(zoneId)
//                        ?.let { ceil(zoneDto.shipmentCount.toDouble() / it).toInt() } ?: 0
                    mView?.startSprinterActivity(zoneId, zone.sprinterList, minSprinterCount)
                }
            }
        else
            for (zone in zoneList) {
                val zoneDto = zone.zoneList.find { it.id == zoneId }
                if (zoneDto != null) {
                    val minSprinterCount = getMinSprinterCount(zoneId)
//                        ?.let { ceil(zoneDto.shipmentCount.toDouble() / it).toInt() } ?: 0
                    mView?.startSprinterActivity(zoneId, zone.sprinterList, minSprinterCount)
                }
            }
    }

    private fun getMinSprinterCount(zoneId: Int): Int {
        val maxTasks = mergedMaxTasksPerSprinterMap[zoneId] ?: maxTasksPerSprinterMap[zoneId]
        val shipmentCount = zoneList.find { it.zoneList.any { it.id == zoneId } }?.zoneList?.sumOf { it.shipmentCount } ?: 0
        return maxTasks?.let { ceil(shipmentCount.toDouble() / it).toInt() } ?: 0
    }

    override fun clearSprinters() {
        RushCore.getInstance().deleteAll(SprinterForZone::class.java)
    }


    override fun setSprinterForZone(zoneId: Int, sprinters: List<SprinterForZone>) {

        for (zone in zoneList) {
            if (zone.zoneList.any { it.id == zoneId }) {
                zone.sprinterList.clear()
                zone.sprinterList.addAll(sprinters)
            }
        }

        if (filteredZoneList.isNotEmpty()) {
            for (zone in filteredZoneList) {
                if (zone.zoneList.any { it.id == zoneId }) {
                    zone.sprinterList.clear()
                    zone.sprinterList.addAll(sprinters)
                }
            }
            mView?.onShipmentsFetched(filteredZoneList)
        } else
            mView?.onShipmentsFetched(zoneList)
    }

    override fun removeSprinterForZone(zoneId: Int, sprinter: SprinterForZone) {
        sprinter.delete()
        for (zone in zoneList) {
            if (zone.zoneList.any { it.id == zoneId }) {
                zone.sprinterList.remove(sprinter)
            }
        }

        if (filteredZoneList.isNotEmpty()) {
            for (zone in filteredZoneList) {
                if (zone.zoneList.any { it.id == zoneId }) {
                    zone.sprinterList.remove(sprinter)
                }
            }
            mView?.onShipmentsFetched(filteredZoneList)
        } else

            mView?.onShipmentsFetched(zoneList)
    }

    override fun applyZoneFilter(s: String) {
        filteredZoneList.clear()
        for (zone in zoneList) {
            if (zone.zoneList.any { it.zoneName!!.contains(s, true) })
                filteredZoneList.add(zone)
        }

        mView?.onShipmentsFetched(filteredZoneList)
    }

    override fun clearFilter() {
        filteredZoneList.clear()
        mView?.onShipmentsFetched(zoneList)
    }

    fun getTimestampStringFOrMonth(date: Int): String {
        val calendar = Calendar.getInstance()
        val value = calendar.clone() as Calendar
        value.add(Calendar.MONTH, -2)
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(value.time).replace(" ", "")
    }

    private fun request(excludedTrips: List<String>): ActiveTrips {
        return ActiveTrips(
            "CREATED",
            getTimestampStringFOrMonth(2),
            getTimestampString2(),
            excludedTrips = excludedTrips
        )
    }

    private fun request2(
        type: String,
        eddStart: String?,
        eddEnd: String?,
        tag: String?,
        excludedTrips: List<String>
    ): ActiveTrips {
        return ActiveTrips(
            status = "CREATED",
            startDate = getTimestampStringForMonth(2),
            endDate = getTimestampString2(),
            type = type,
            eddStartDate = eddStart,
            eddEndDate = eddEnd,
            tag = tag,
            excludedTrips = excludedTrips
        )
    }

    override fun createTrips() {
        compositeSubscription?.add(
            sortationApiInteractor.createSmartTripV2(getShipmentsFromZone())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe({ _ ->
                    mView?.onCreateSuccess()
                }, { error ->
                    mView?.onFailure(error)
                })
        )
    }

    private fun tripRequest(): ZoneSprinterDetailsRequest {
        return ZoneSprinterDetailsRequest(zoneList)

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

    private fun getShipmentsFromZone(): SmartTripCreateRequest {
        val list = mutableListOf<SprinterShipmentList>()
        zoneList.filter { !it.sprinterList.isNullOrEmpty() }.forEach { zoneSprinterDTO ->
            val shipmentList = mutableListOf<String>()
            val iterator = zoneSprinterDTO.zoneList.listIterator()
            while(iterator.hasNext()){
                val zoneList = iterator.next()
                shipmentList.addAll(filteredShipments.filter { it.zoneName == zoneList.zoneName }.map { it.shipmentId })
                 }
            val sprinterShipmentList = SprinterShipmentList(zoneSprinterDTO.sprinterList,shipmentList, zoneSprinterDTO.zoneList.map { it.id.toLong() }.toMutableList())
            list.add(sprinterShipmentList)
            }

        return SmartTripCreateRequest(list)

        }

    override fun cancelTripForZone(tripProcessId: Int) {

        compositeSubscription?.add(sortationApiInteractor.cancelSmartTrip(tripProcessId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                mView?.onCancelSuccess()
            }, {

                mView?.onFailure(it)
            })
        )
    }

}