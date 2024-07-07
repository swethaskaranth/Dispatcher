package com.goflash.dispatch.features.lastmile.tripCreation.presenter.impl

import android.content.Context
import co.uk.rushorm.core.RushSearch
import com.goflash.dispatch.data.ReceiveCdsCash
import com.goflash.dispatch.data.TaskListDTO
import com.goflash.dispatch.di.interactor.SortationApiInteractor
import com.goflash.dispatch.features.lastmile.tripCreation.presenter.LastMileSummaryPresenter
import com.goflash.dispatch.features.lastmile.tripCreation.view.LastMileSummaryView
import com.goflash.dispatch.type.BagStatus
import com.goflash.dispatch.util.taskListClear
import com.google.firebase.crashlytics.FirebaseCrashlytics
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription
import kotlin.math.roundToInt

class LastMileSummaryPresenterImpl (private val sortationApiInteractor: SortationApiInteractor) : LastMileSummaryPresenter {

    private var summaryView : LastMileSummaryView? = null

    private var compositeSubscription: CompositeSubscription? = null

    private var mList = mutableListOf<TaskListDTO>()

    private val completeTasks: LinkedHashMap<String,List<TaskListDTO>> = LinkedHashMap()
    private val inCompleteTasks: LinkedHashMap<String,List<TaskListDTO>> = LinkedHashMap()

    private var trip : TaskListDTO? = null

    override fun onAttachView(context: Context, summaryView: LastMileSummaryView) {
        this.summaryView = summaryView
        compositeSubscription = CompositeSubscription()
    }

    override fun onDetachView() {
        if (this.summaryView == null)
            return
        summaryView = null
        compositeSubscription = null
    }

    override fun getTasksByTripId(tripId: String, receiveCdsCash: ReceiveCdsCash?) {
        compositeSubscription?.add(sortationApiInteractor.getShipmentsforTrip(tripId.toLong())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                mList.clear()
                taskListClear()
                getTasksByStatus(it, tripId,receiveCdsCash)
            }, {
                summaryView?.onFailure(it)
            }))
    }

    private fun getTasksByStatus(it: List<TaskListDTO>, tripId: String,  receiveCdsCash: ReceiveCdsCash?) {

        trip = RushSearch().whereEqual("tripId", tripId).findSingle(TaskListDTO::class.java)

        if(trip == null)
            it.forEach { i ->
                i.tripId = tripId
                i.save()
            }

        mList.addAll(it)

        if(mList.size > 0) {
            val compList = mList.sortedBy { it.priority }.filter { it.status == BagStatus.COMPLETED.name }.groupBy { it.status }

            val comp = compList[BagStatus.COMPLETED.name]?.groupBy { it.type }
            if(comp != null)
                completeTasks.putAll(comp)

            val inCompList = mList.sortedBy { it.priority }.filter { it.status != BagStatus.COMPLETED.name }.groupBy { it.status }
            inCompleteTasks.putAll(inCompList)


            summaryView?.onTasksFetched(completeTasks,inCompleteTasks)

            getCashInHand(receiveCdsCash)

        }else
            summaryView?.hideProgressBar()
    }

    override fun getCashInHand( receiveCdsCash: ReceiveCdsCash?)  {

        val list = mList.filter { it.status == BagStatus.COMPLETED.name }
            .filter { it.actualPaymentType != null && it.actualPaymentType == "COD" }

        FirebaseCrashlytics.getInstance().log("Name:- $list")

        var cashInHand  = 0.0
        list.map {
            cashInHand += it.shipmentValue
        }

        summaryView?.onCashFetched(cashInHand.roundToInt(), (receiveCdsCash?.total
            ?: 0.0).roundToInt())
    }

}