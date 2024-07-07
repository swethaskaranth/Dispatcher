package com.goflash.dispatch.features.bagging.presenter.impl

import android.content.Context
import android.net.Uri
import com.goflash.dispatch.app_constants.reference_id
import com.goflash.dispatch.data.PackageDto
import com.goflash.dispatch.data.ScannedOrder
import com.goflash.dispatch.data.SortOrder
import com.goflash.dispatch.model.CommonRequest
import com.goflash.dispatch.di.interactor.SortationApiInteractor
import com.goflash.dispatch.features.bagging.presenter.SortPresenter
import com.goflash.dispatch.features.bagging.view.SortationView
import com.goflash.dispatch.presenter.impl.LoginPresenterImpl
import com.goflash.dispatch.type.PackageStatus
import com.goflash.dispatch.util.PreferenceHelper
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription

class SortActivityPresenterImpl(private val sortationApiInteractor: SortationApiInteractor) : SortPresenter {

    private val TAG = LoginPresenterImpl::class.java.simpleName

    private var sortView: SortationView? = null

    private var compositeSubscription: CompositeSubscription? = null

    var scannedPackage: PackageDto? = null

    var scannedOrder: ScannedOrder? = null

    var orderScanned = false


    override fun onAttachView(context: Context, view: SortationView) {
        this.sortView = view
        compositeSubscription = CompositeSubscription()

    }

    override fun onDetachView() {
        if (this.sortView == null)
            return
        sortView = null
        compositeSubscription?.unsubscribe()
        compositeSubscription = null

    }

    override fun onBinScan(barCode: String) {
        if(PreferenceHelper.singleScanSortation){
            compositeSubscription?.add(sortationApiInteractor.getSortationBinForSingleScan(SortOrder(barCode))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({result ->
                    result.scannedOrders[0].scannedBarcode = barCode
                    sortView?.displayScannedOrders(result.scannedOrders[0])

                },{
                    sortView?.onFailure(it)
                })
            )
        }
        else{
            if (!orderScanned) {
                compositeSubscription?.add(
                    sortationApiInteractor.getSortationBinV4(SortOrder(barCode))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ result ->
                            orderScanned = true
                            val order = result.scannedOrders[0]
                            if (order.status == PackageStatus.CANCELLED.name)
                                sortView?.onSuccessCancelledOrderScan(result)
                            else {
                                scannedPackage = result
                                scannedOrder = scannedPackage?.scannedOrders?.get(0)
                                if (scannedOrder != null)
                                    sortView?.showBinName(scannedOrder!!.binNumber, barCode)
                                else
                                    sortView?.onFailure(Throwable("Something went wrong. Please try again later."))
                            }
                        }, { error ->
                            sortView?.onFailure(error)
                        })
                )
            }else{
                if (barCode == scannedOrder?.binNumber) {
                    compositeSubscription?.add(sortationApiInteractor.updateBin(scannedPackage!!)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            orderScanned = false
                            sortView?.onSuccessBinScan()
                        }, { error ->
                            sortView?.onFailure(error)
                        }))
                } else
                    sortView?.onFailure(Throwable("Invalid Bin"))

            }
        }


    }

    override fun reInitialize() {
        orderScanned = false
    }

}