package com.goflash.dispatch.features.dispatch.presenter.impl

import android.content.Context
import co.uk.rushorm.core.RushCore
import co.uk.rushorm.core.RushSearch
import com.goflash.dispatch.model.CommonRequest
import com.goflash.dispatch.data.PackageDto
import com.goflash.dispatch.data.ScannedOrder
import com.goflash.dispatch.di.interactor.SortationApiInteractor
import com.goflash.dispatch.features.bagging.presenter.impl.ScanSortBinPresenterImpl
import com.goflash.dispatch.features.dispatch.presenter.ScanDispatchBinPresenter
import com.goflash.dispatch.features.dispatch.view.ScanDispatchBinView
import com.goflash.dispatch.model.DispatchShipmentRequest
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription

class ScanDispatchBinPresenterImpl(private val sortationApiInteractor: SortationApiInteractor) :
    ScanDispatchBinPresenter {

    private val TAG = ScanSortBinPresenterImpl::class.java.simpleName

    private var scanDispatchBinView: ScanDispatchBinView? = null

    private var compositeSubscription: CompositeSubscription? = null

    private var singleOrderscanned: Boolean = false

    override fun onAttachView(context: Context, view: ScanDispatchBinView) {
        this.scanDispatchBinView = view
        compositeSubscription = CompositeSubscription()
    }

    override fun onDetachView() {
        if (this.scanDispatchBinView == null)
            return
        scanDispatchBinView = null
        compositeSubscription?.unsubscribe()
        compositeSubscription = null

    }

    override fun dispatchShipments(scannedOrders: List<ScannedOrder>) {
        compositeSubscription?.add(
            sortationApiInteractor.dispatchShipmentsV2(getReferenceIdList(scannedOrders))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    if (result.scannedOrders == null || result.scannedOrders.size == 0) {
                        RushCore.getInstance().clearDatabase()
                        scanDispatchBinView?.onSuccess("Orders Dispatched Successfully.")
                    } else {
                        updateCancelledOrders(result)
                        scanDispatchBinView?.dispatchCancelledOrdersPresent()
                    }

                }, { error: Throwable? ->
                    scanDispatchBinView?.onFailure(error)
                })
        )
    }

    override fun initiateDispatch(barcode: String) {
        compositeSubscription?.add(
            sortationApiInteractor.initiateDispatch(barcode)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    if (result.scannedOrders == null || result.scannedOrders.size == 0) {
                        scanDispatchBinView?.onFailure(Throwable("No orders present in the bin."))
                    } else {

                        singleOrderscanned = result.scannedOrders.size == 1 && result.scannedOrders[0].referenceId == barcode || result.scannedOrders[0].packageId == barcode || result.scannedOrders[0].lbn == barcode

                        saveToLocal(result)
                        scanDispatchBinView?.onSuccess(isPackageDispatchable(), singleOrderscanned)

                    }


                }, { error: Throwable? ->
                    scanDispatchBinView?.onFailure(error)
                })
        )
    }

    private fun getReferenceIdList(scannedOrders: List<ScannedOrder>): ArrayList<DispatchShipmentRequest> {
        val request = ArrayList<DispatchShipmentRequest>()
        for (order in scannedOrders)
            request.add(DispatchShipmentRequest(order.shipmentId))

        return request
    }

    private fun isPackageDispatchable(): Boolean {
        var dispatchable = true

        val scannedOrders = RushSearch().find(ScannedOrder::class.java)

        for (order in scannedOrders) {
            if (!order.isDispatchable) {
                dispatchable = false

            }
        }

        return dispatchable

    }

    private fun saveToLocal(packageDto: PackageDto) {

        var savedPackage = RushSearch().findSingle(PackageDto::class.java)
        if (savedPackage == null) {
            packageDto.save()
            return
        }

        for (order in packageDto.scannedOrders)
            if (!savedPackage.scannedOrders.contains(order)) {
                if (order.isDispatchable && !savedPackage.scannedOrders.get(0).binNumber.equals(
                        order.binNumber
                    )
                )
                    scanDispatchBinView?.onFailure(Throwable("Invalid bin"))
                else
                    savedPackage.scannedOrders.add(order)


            }
        savedPackage.save()
    }

    private fun updateCancelledOrders(packageDto: PackageDto) {
        val orders = RushSearch().find(ScannedOrder::class.java)

        for (order in orders)
            if (packageDto.scannedOrders.contains(order)) {
                val cancelledOrder =
                    packageDto.scannedOrders.get(packageDto.scannedOrders.indexOf(order))
                order.isDispatchable = cancelledOrder.isDispatchable
                order.status = cancelledOrder.status
                order.binNumber = cancelledOrder.binNumber
                order.save()
            }
    }

    private fun getCancelledOrderBin() {

    }

    override fun getPackage() {
        val packageDto = RushSearch().findSingle(PackageDto::class.java)
        if (packageDto != null)
            scanDispatchBinView?.onPackageFetched(packageDto)
        else
            scanDispatchBinView?.onFailure(Throwable("No Package Found"))
    }


}