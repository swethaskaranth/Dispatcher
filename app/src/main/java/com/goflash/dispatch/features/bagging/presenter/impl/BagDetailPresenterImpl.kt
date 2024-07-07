package com.goflash.dispatch.features.bagging.presenter.impl

import android.content.Context
import android.content.Intent
import com.goflash.dispatch.api_services.SessionService
import com.goflash.dispatch.app_constants.close_Bag
import com.goflash.dispatch.app_constants.scannedPackageList
import com.goflash.dispatch.data.BagDTO
import com.goflash.dispatch.data.CloseBinRequest
import com.goflash.dispatch.data.PackageDto
import com.goflash.dispatch.data.RouteIdBasedTripDto
import com.goflash.dispatch.di.interactor.SortationApiInteractor
import com.goflash.dispatch.features.bagging.presenter.BagDetailPresenter
import com.goflash.dispatch.features.bagging.view.BagDetailView
import com.goflash.dispatch.util.PreferenceHelper
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription

class BagDetailPresenterImpl(private val sortationApiInteractor: SortationApiInteractor) : BagDetailPresenter {

    private var bagDetailView: BagDetailView? = null
    private var compositeSubscription : CompositeSubscription? = null

    private var scannedPackage : PackageDto? = null


    override fun onAttachView(context: Context, view: BagDetailView) {
        this.bagDetailView = view
        compositeSubscription = CompositeSubscription()

    }

    override fun onDetachView() {
        if(bagDetailView == null)
            return
        bagDetailView = null
        compositeSubscription?.unsubscribe()
        compositeSubscription = null
    }

    override fun sendIntent(intent: Intent) {
        if (intent.hasExtra(scannedPackageList))
            scannedPackage = intent.getParcelableExtra<PackageDto>(scannedPackageList) as PackageDto
        if(intent.hasExtra(close_Bag)) {
            val bag_created = intent.getBooleanExtra(close_Bag, false)
            if (bag_created)
                bagDetailView?.restoreBagState()
        }
    }

    override fun getShipmentCount() {
        if(scannedPackage != null) {
            bagDetailView?.showShipmentCount(scannedPackage?.scannedOrders?.size)
            if (scannedPackage?.sprinterBin == true)
                bagDetailView?.setTripDetails(
                    scannedPackage?.scannedOrders?.get(0)?.binNumber ?: ""
                )
            else
                bagDetailView?.setBagDestination(
                    scannedPackage?.scannedOrders?.get(0)?.binBagDestination ?: ""
                )
        }
    }

    override fun onBarcodeScanned(barcode: String) {
        val bag = BagDTO()
        bag.bagId = barcode
        bag.currentNodeName = PreferenceHelper.assignedAssetName
        bag.currentNodeId = PreferenceHelper.assignedAssetId
        bag.shipmentList = scannedPackage!!.scannedOrders

        compositeSubscription?.add(sortationApiInteractor.createBag(bag)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    bagDetailView?.onSuccess(it.string())
                },{error ->
                    bagDetailView?.onFailure(error)

                }))
    }

    override fun getShipmentList() {
        bagDetailView?.goToShipmentsActivity(scannedPackage!!)
    }

    override fun onProceedClicked() {
            compositeSubscription?.add(sortationApiInteractor.closeBin( RouteIdBasedTripDto(scannedPackage?.tripId!!, scannedPackage?.scannedOrders!!.map { it.referenceId }))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    bagDetailView?.goToHomeActivity()
                }, { error ->
                    bagDetailView?.onFailure(error)

                })
            )
    }

    override fun shouldShowAlert(): Boolean = scannedPackage?.sprinterBin == true


}