package com.goflash.dispatch.features.bagging.presenter.impl

import android.content.Context
import android.content.Intent
import com.goflash.dispatch.app_constants.close_Bag
import com.goflash.dispatch.data.PackageDto
import com.goflash.dispatch.data.ScannedOrder
import com.goflash.dispatch.di.interactor.SortationApiInteractor
import com.goflash.dispatch.features.bagging.presenter.ScanSotrBinPresenter
import com.goflash.dispatch.features.bagging.view.ScanSortBinView
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription

class ScanSortBinPresenterImpl(private val sortationApiInteractor: SortationApiInteractor) :
    ScanSotrBinPresenter {

    private val TAG = ScanSortBinPresenterImpl::class.java.simpleName

    private var scanView: ScanSortBinView? = null

    private var compositeSubscription: CompositeSubscription? = null

    var scannedPackage: PackageDto? = null

    var scannedOrder: ScannedOrder? = null

    var closeBag: Boolean = false

    override fun onAttachView(context: Context, view: ScanSortBinView) {
        this.scanView = view
        compositeSubscription = CompositeSubscription()
    }

    override fun onDetachView() {
        if (this.scanView == null)
            return
        scanView = null
        compositeSubscription?.unsubscribe()
        compositeSubscription = null

    }

    override fun sendIntent(intent: Intent) {

        closeBag = intent.getBooleanExtra(close_Bag, false)
        if (!closeBag) {

            scannedPackage = intent.getParcelableExtra<PackageDto>(com.goflash.dispatch.app_constants.scannedPackage) as PackageDto
            scannedOrder = scannedPackage?.scannedOrders?.get(0)

            if (scannedOrder!!.packageId != null && scannedOrder!!.packageId.isNotEmpty())
                scanView?.showBinName(scannedOrder!!.binNumber, scannedOrder!!.packageId)
            else
                scanView?.showBinName(scannedOrder!!.binNumber,scannedOrder!!.referenceId)
        } else {
            scanView?.hideOrderLayout()
        }
    }

    override fun onBarcodeScanned(barcode: String) {
        if (!closeBag) {
            if (barcode == scannedOrder?.binNumber) {
                onBinScan(scannedPackage!!)
            } else
                scanView?.onFailure(Throwable("Invalid Bin"))
        } else {
            compositeSubscription?.add(
                sortationApiInteractor.getShipmentsinBin(barcode)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        for (order in it.scannedOrders)
                            order.binNumber = barcode
                        scanView?.takeToBagDetailScreen(it)
                    }, { error: Throwable? ->
                        scanView?.onFailure(error)
                    })
            )

        }
    }

    override fun onBinScan(PackageDto: PackageDto) {
        compositeSubscription?.add(
            sortationApiInteractor.updateBin(PackageDto)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    scanView?.onSuccess()
                }, { error: Throwable? ->
                    scanView?.onFailure(error)
                })
        )
    }


}