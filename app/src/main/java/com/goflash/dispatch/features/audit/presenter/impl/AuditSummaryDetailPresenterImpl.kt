package com.goflash.dispatch.features.audit.presenter.impl

import android.content.Context
import android.content.Intent
import com.goflash.dispatch.app_constants.audit_id
import com.goflash.dispatch.app_constants.end_time
import com.goflash.dispatch.app_constants.start_time
import com.goflash.dispatch.di.interactor.SortationApiInteractor
import com.goflash.dispatch.features.audit.presenter.AuditSummaryDetailPresenter
import com.goflash.dispatch.features.audit.view.AuditSummaryDetailView
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription
import java.text.SimpleDateFormat
import java.util.*

class AuditSummaryDetailPresenterImpl(private val sortationApiInteractor: SortationApiInteractor) :
    AuditSummaryDetailPresenter {

    private var TAG = AuditSummaryDetailPresenterImpl::class.java.simpleName

    private var auditSummaryView : AuditSummaryDetailView? = null

    private var compositeSubscription : CompositeSubscription? = null

    private var auditId = 0L

    private var startTime : String = ""

    private var endTime : String = ""

    override fun onAttachView(context: Context, view: AuditSummaryDetailView) {
        this.auditSummaryView = view
        compositeSubscription = CompositeSubscription()
    }

    override fun onDetachView() {
        if(auditSummaryView == null)
            return
        auditSummaryView = null
        compositeSubscription?.unsubscribe()
        compositeSubscription = null
    }

    override fun sendIntent(intent: Intent) {
        auditId = intent.getLongExtra(audit_id,0L)

        startTime = intent.getStringExtra(start_time)?: ""
        endTime = intent.getStringExtra(end_time)?: ""

        setStartAndEndTimes()
        getAuditDetail()
    }

    private fun getAuditDetail(){
        compositeSubscription?.add(sortationApiInteractor.getSummaryForId(auditId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({summary ->
                auditSummaryView?.setNameAndAsset(summary.userName,summary.assetName)
                auditSummaryView?.setBagCount(summary.bagCount.scannedCount,summary.bagCount.expectedCount)
                auditSummaryView?.setBagShortAndExtraCount(summary.bagCount.missedCount,summary.bagCount.extraCount)

                auditSummaryView?.setShipmentCount(summary.shipmentCount.scannedCount,summary.shipmentCount.expectedCount)
                auditSummaryView?.setShipmentShortAndExtraCount(summary.shipmentCount.missedCount,summary.shipmentCount.extraCount)

            },{error ->
                auditSummaryView?.onFailure(error)

            }))
    }

    private fun setStartAndEndTimes(){
        auditSummaryView?.setStartTime(getTimeFromISODate(startTime))
        auditSummaryView?.setEndTime(getTimeFromISODate(endTime))
    }

    private fun getTimeFromISODate(dateStr: String): String {

        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        val cal = Calendar.getInstance()
        cal.time = format.parse(dateStr)


        val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm a")
        val dateforrow = dateFormat.format(cal.time)

        return dateforrow

    }

}