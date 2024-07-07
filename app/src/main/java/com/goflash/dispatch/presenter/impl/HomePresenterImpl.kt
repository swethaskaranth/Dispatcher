package com.goflash.dispatch.presenter.impl

import android.content.Context
import co.uk.rushorm.core.RushSearch
import com.goflash.dispatch.api_services.SessionService
import com.goflash.dispatch.data.PackageDto
import com.goflash.dispatch.di.interactor.SortationApiInteractor
import com.goflash.dispatch.presenter.HomePresenter
import com.goflash.dispatch.presenter.views.HomeView
import com.goflash.dispatch.util.PreferenceHelper
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription
import java.text.SimpleDateFormat
import java.util.*

class HomePresenterImpl(private val sortationApiInteractor: SortationApiInteractor) :
    HomePresenter {

    private val TAG = HomePresenterImpl::class.java.simpleName

    private var homeView: HomeView? = null

    private var compositeSubscription: CompositeSubscription? = null

    private lateinit var from: Date
    private lateinit var to: Date

    override fun onAttachView(context: Context, view: HomeView) {
        this.homeView = view
        compositeSubscription = CompositeSubscription()
    }

    override fun onDetachView() {
        if (this.homeView == null)
            return
        homeView = null
        compositeSubscription?.unsubscribe()
        compositeSubscription = null
    }

    override fun checkIfDispatchStarted() {
        val packageDto = RushSearch().findSingle(PackageDto::class.java)

        if (packageDto != null)
            if (isPackageDispatchable(packageDto))
                homeView?.takeToDispatchBinScreen(packageDto, isPackageDispatchable(packageDto))
            else
                homeView?.takeToCancelledScreen()
        else {
            /* val bagDto = RushSearch().find(BagDTO::class.java)

             if (bagDto != null && bagDto.size > 0)
                 homeView?.takeToDispatchBagScreen()
             else*/
            homeView?.takeToDispatchScreen()
        }


    }

    override fun getSummary() {
        getFromAndToDates()
        compositeSubscription?.add(
            sortationApiInteractor.getSummary(getDateString(from), getDateString(to))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ summary ->
                    SessionService.auditActive = summary.activeAudit == true

                    homeView?.showBaggedCount(
                        summary.sortBagged?.bag
                            ?: 0, summary.sortBagged?.shipment ?: 0
                    )
                    homeView?.showDispatchedCount(summary.dispatched?.trips ?: 0)
                    homeView?.showReceivedCount(
                        summary.received?.bag
                            ?: 0, summary.received?.shipment ?: 0
                    )
                }, { error ->
                    homeView?.showBaggedCount(0, 0)
                    homeView?.showDispatchedCount(0)
                    homeView?.showReceivedCount(0, 0)
                    homeView?.onFailure(error)

                })
        )
    }

    private fun getFromAndToDates() {
        val cal1 = Calendar.getInstance()

        cal1.set(Calendar.HOUR_OF_DAY, 0)
        cal1.set(Calendar.MINUTE, 0)
        cal1.set(Calendar.SECOND, 0)
        from = cal1.time

        cal1.add(Calendar.DATE, 1)

        to = cal1.time

    }

    private fun getDateString(date: Date): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        return dateFormat.format(date)
    }

    private fun isPackageDispatchable(packageDto: PackageDto): Boolean {

        val nonDispatchableList = packageDto.scannedOrders.filter { order -> !order.isDispatchable }

        return (nonDispatchableList.isEmpty())
    }

    override fun getInwardRuns() {
        compositeSubscription?.add(
            sortationApiInteractor.getInwardRuns(PreferenceHelper.dataForNumDays)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    if(response.pending != null) {
                        if (response.pending.createdBy == SessionService.userId)
                            homeView?.takeToReceiveScanScreen(response.pending.id)
                        else
                            homeView?.takeToReceivingListScreen(true)
                    }
                    else
                        homeView?.takeToReceivingListScreen(false)

                },{
                    homeView?.onFailure(it)
                })
        )
    }


}