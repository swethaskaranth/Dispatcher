package com.goflash.dispatch.features.audit.presenter.impl

import android.content.Context
import android.content.Intent
import com.goflash.dispatch.app_constants.audit_id
import com.goflash.dispatch.app_constants.bag_count
import com.goflash.dispatch.app_constants.shipment_count
import com.goflash.dispatch.di.interactor.SortationApiInteractor
import com.goflash.dispatch.model.AuditItemsRequest
import com.goflash.dispatch.model.Error
import com.goflash.dispatch.features.audit.presenter.AuditScanPresenter
import com.goflash.dispatch.features.audit.view.AuditScanView
import com.goflash.dispatch.util.gson
import retrofit2.adapter.rxjava.HttpException
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription
import java.util.regex.Pattern

class AuditScanPresenterImpl(private val sortationApiInteractor: SortationApiInteractor) :
    AuditScanPresenter {


    private val TAG = AuditScanPresenterImpl::class.java.simpleName

    private var homeView: AuditScanView? = null

    private var compositeSubscription: CompositeSubscription? = null

    private var bagCount = 0L

    private var shipmentCount = 0L

    private var auditId = 0L

    private var bagList = mutableListOf<String>()

    private var shipmentList = mutableListOf<String>()

    override fun onAttachView(context: Context, view: AuditScanView) {
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

    override fun sendIntent(intent: Intent) {
        bagCount = intent.getLongExtra(bag_count,0L)
        shipmentCount = intent.getLongExtra(shipment_count,0L)

        auditId = intent.getLongExtra(audit_id,0L)

        homeView?.showCount(bagCount,shipmentCount)
    }

    override fun onBarcodeScanned(barcode: String) {
        val pattern = Pattern.compile("[$&+,:;=\\\\?@#|/'<>.^*()%!]|-{2,}");

         //Pattern.compile("^[A-Za-z0-9]-{1}")
        if(pattern.matcher(barcode).find() || barcode.isEmpty()){
            homeView?.onFailure(Throwable("Scanned barcode $barcode contains special characters. Please scan again."))
            return
        }

        if(bagList.contains(barcode) || shipmentList.contains(barcode)){
            homeView?.onFailure(Throwable("Already Scanned"))
            return
        }
        compositeSubscription?.add(sortationApiInteractor.addAuditItems(AuditItemsRequest(auditId,false,
            listOf(barcode)))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (barcode.startsWith("TH",true))
                    bagList.add(barcode)
                else
                    shipmentList.add(barcode)
                homeView?.showCount(bagCount+bagList.size,shipmentCount+shipmentList.size)

            },{error ->
                checkIfAuditInactive(error)
            }))
    }

    private fun checkIfAuditInactive(error: Throwable?) {
        if (error == null)
            return

        if (error is HttpException)
            httpErrorMessage(error)
        else
            homeView?.onFailure(error)

    }

    private fun httpErrorMessage(error: Throwable) {

        val res = (error as HttpException).response()
        val code = res.code()
        if (code == 400) {
            val response = res.errorBody()

            val body = response?.string()
            if (body != null) {

                val e = gson.fromJson(body, Error::class.java)
                if (e.message == "Invalid/Inactive auditId given in the request")
                    homeView?.showAuditInactive()
                else
                    homeView?.onFailure(error)
            }else{
                homeView?.onFailure(error)
            }
        }else
            homeView?.onFailure(error)

    }

    override fun completeAudit() {
        val list = mutableListOf<String>()
        list.addAll(bagList)
        list.addAll(shipmentList)
        compositeSubscription?.add(sortationApiInteractor.addAuditItems(AuditItemsRequest(auditId,true,list))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                homeView?.onSuccess()

            },{ error ->
                homeView?.onFailure(error)
            }))
    }



}