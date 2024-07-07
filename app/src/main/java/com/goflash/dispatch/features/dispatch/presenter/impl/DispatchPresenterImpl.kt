package com.goflash.dispatch.features.dispatch.presenter.impl

import android.content.Context
import co.uk.rushorm.core.RushCore
import co.uk.rushorm.core.RushSearch
import com.goflash.dispatch.data.BagDTO
import com.goflash.dispatch.data.PackageDto
import com.goflash.dispatch.data.ScannedOrder
import com.goflash.dispatch.di.interactor.SortationApiInteractor
import com.goflash.dispatch.features.dispatch.presenter.DispatchPresenter
import com.goflash.dispatch.features.dispatch.view.DispatchView
import com.goflash.dispatch.type.ShipmentType
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription

class DispatchPresenterImpl(private val sortationApiInteractor: SortationApiInteractor) : DispatchPresenter {

    private var dispatchView: DispatchView? = null

    private var compositeSubscription: CompositeSubscription? = null

    override fun onAttachView(context: Context, view: DispatchView) {
        this.dispatchView = view
        compositeSubscription = CompositeSubscription()

    }

    override fun onDetachView() {

        if (this.dispatchView == null)
            return
        dispatchView = null
        compositeSubscription?.unsubscribe()
        compositeSubscription = null
    }

    override fun initiateDispatch(barcode: String) {
        compositeSubscription?.add(sortationApiInteractor.initiateDispatch(barcode)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    if (result?.scannedOrders?.get(0)?.shipmentType?.equals(ShipmentType.BAG.name) == true) {
                        saveBagToLocal(result)
                        dispatchView?.takeToDispatchBagActivity()
                    } else {
                        saveToLocal(result)
                        processPackage(barcode)
                    }
                }, { error: Throwable? ->
                    dispatchView?.onFailure(error)
                }))

    }


    private fun processPackage(barcodeScanned: String) {
        var dispatchable = true

        val scannedOrders = RushSearch().find(ScannedOrder::class.java)

        for (order in scannedOrders) {
            if (!order.isDispatchable) {
                dispatchable = false

            }
        }

        if (!dispatchable && scannedOrders.size == 1)
            dispatchView?.takeToCancelledActivity(scannedOrders[0].referenceId == barcodeScanned || scannedOrders[0].packageId == barcodeScanned || scannedOrders[0].lbn == barcodeScanned)
        else
            dispatchView?.onSuccessOrderScan(dispatchable)

    }



    private fun saveToLocal(packageDto: PackageDto) {

        val savedPackage = RushSearch().findSingle(PackageDto::class.java)
        if (savedPackage == null) {
            packageDto.save()
            return
        }

        for (order in packageDto.scannedOrders)
            if (!savedPackage.scannedOrders.contains(order))
                if (savedPackage.scannedOrders.get(0).binNumber.equals(order.binNumber))
                    savedPackage.scannedOrders.add(order)
                else
                    dispatchView?.onFailure(Throwable("Invalid bin"))
        savedPackage.save()
    }

    private fun saveBagToLocal(packageDto: PackageDto){

        RushCore.getInstance().deleteAll(BagDTO::class.java)

        val bag = RushSearch().whereEqual("bagId",packageDto.scannedOrders.get(0).referenceId).findSingle(
            BagDTO::class.java)
        if(bag == null){
            val bagDto = BagDTO()
            bagDto.bagId = packageDto.scannedOrders.get(0).referenceId
            bagDto.save()
        }

    }


}