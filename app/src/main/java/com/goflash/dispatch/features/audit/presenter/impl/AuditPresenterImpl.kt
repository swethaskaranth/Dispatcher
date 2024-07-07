package com.goflash.dispatch.features.audit.presenter.impl

import android.content.Context
import com.goflash.dispatch.api_services.SessionService
import com.goflash.dispatch.di.interactor.SortationApiInteractor
import com.goflash.dispatch.features.audit.presenter.AuditPresenter
import com.goflash.dispatch.features.audit.view.AuditView
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription
import java.text.SimpleDateFormat
import java.util.*

class AuditPresenterImpl(private val sortationApiInteractor: SortationApiInteractor) :
    AuditPresenter {


    private val TAG = AuditPresenterImpl::class.java.simpleName

    private var homeView: AuditView? = null

    private var compositeSubscription: CompositeSubscription? = null

    override fun onAttachView(context: Context, view: AuditView) {
        this.homeView = view
        compositeSubscription = CompositeSubscription()
        checkIfAuditActive()
    }

    override fun onDetachView() {
        if (this.homeView == null)
            return
        homeView = null
        compositeSubscription?.unsubscribe()
        compositeSubscription = null
    }

    override fun beginAudit() {
        compositeSubscription?.add(sortationApiInteractor.createAudit()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({audit ->
                homeView?.onSuccess(audit.count.user.bag,audit.count.user.shipment,audit.auditId)

            },{error ->
                homeView?.onFailure(error)

            }))
    }

    private fun checkIfAuditActive(){
        if(SessionService.auditActive)
            compositeSubscription?.add(sortationApiInteractor.getCurrentAuditDetails()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({audit ->
                    homeView?.showActiveAuditData(audit.userName,getTimeFromISODate(audit.createdOn))

                },{error ->
                    homeView?.onFailure(error)

                }))
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