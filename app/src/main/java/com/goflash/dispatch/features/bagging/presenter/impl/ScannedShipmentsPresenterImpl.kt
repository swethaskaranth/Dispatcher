package com.goflash.dispatch.features.bagging.presenter.impl

import android.content.Context
import android.content.Intent
import com.goflash.dispatch.R
import com.goflash.dispatch.app_constants.close_Bag
import com.goflash.dispatch.app_constants.scannedBag
import com.goflash.dispatch.app_constants.scannedPackageList
import com.goflash.dispatch.data.BagDTO
import com.goflash.dispatch.data.PackageDto
import com.goflash.dispatch.data.ScannedOrder
import com.goflash.dispatch.di.interactor.SortationApiInteractor
import com.goflash.dispatch.features.bagging.presenter.ScannedShipmentsPresenter
import com.goflash.dispatch.presenter.views.CancelledRowView
import com.goflash.dispatch.features.bagging.view.ScannedShipmentsView
import rx.subscriptions.CompositeSubscription

class ScannedShipmentsPresenterImpl(private val sortationApiInteractor: SortationApiInteractor) :
    ScannedShipmentsPresenter {

    private val TAG: String = ScannedShipmentsPresenterImpl::class.java.name

    private var scannedShipmentsView: ScannedShipmentsView? = null

    private var compositeSubscription: CompositeSubscription? = null

    private var scannedPackage: PackageDto? = null
    private var bagDto: BagDTO? = null

    private var scannedShipments = mutableListOf<ScannedOrder>()

    private var lastRemovedShipment: ScannedOrder? = null

    private var lastRemovedPosition: Int = -1

    private var bag_created = false

    private var context: Context? = null

    override fun onAttachView(context: Context, view: ScannedShipmentsView) {
        this.scannedShipmentsView = view
        this.context = context
        compositeSubscription = CompositeSubscription()
    }

    override fun onDetachView() {
        if (scannedShipmentsView == null)
            return
        scannedShipmentsView = null
        compositeSubscription?.unsubscribe()
        compositeSubscription = null
    }

    override fun sendIntent(intent: Intent) {
        if (intent.hasExtra(scannedPackageList)) {
            scannedPackage = intent.getParcelableExtra<PackageDto>(scannedPackageList) as PackageDto
            scannedShipments.addAll(scannedPackage!!.scannedOrders)
        }
        if (intent.hasExtra(scannedBag)) {
            bagDto = intent.getParcelableExtra<PackageDto>(scannedBag) as BagDTO
            scannedShipments.addAll(bagDto!!.shipmentList)
            scannedShipmentsView?.hideScanner()
        }
        if (intent.hasExtra(close_Bag))
            bag_created = intent.getBooleanExtra(close_Bag, false)
        getBinAndCount()

    }


    private fun getBinAndCount() {
        if (scannedShipments.size > 0)
            scannedShipmentsView?.setBinNumnberAndCount(
                scannedShipments.get(0).binNumber
                    ?: "", scannedShipments.size
            )
    }

    override fun getCount(): Int {
        return scannedShipments.size
    }

    override fun onBindCanceeldRowView(position: Int, holder: CancelledRowView) {
        val shipment = scannedShipments[position]

        if (shipment.packageId != null && shipment.packageId.isNotEmpty())
            holder.setOrderId(
                String.format(
                    context!!.getString(R.string.package_id_label),
                    shipment.packageId
                )
            )
        else
            holder.setOrderId(
                String.format(
                    context!!.getString(R.string.reference_id_label),
                    shipment.referenceId
                )
            )
        holder.setLBN(String.format(context!!.getString(R.string.lbn_label), shipment.lbn))

       /* if (shipment.packageId != null && shipment.packageId.isNotEmpty())
            holder.setOrderId(shipment.packageId)
        else
            holder.setOrderId(shipment.referenceId)*/
        holder.setLBN(shipment.lbn)
    }

    override fun onBarcodeScanned(barcode: String) {
        if (bag_created) {
            scannedShipmentsView?.onFailure(Throwable("Bag already created. Shipment cannot be removed"))
            return
        }
        if (bagDto != null)
            return

        if (scannedShipments.size == 1) {
            scannedShipmentsView?.onFailure(Throwable("Cannot remove the only shipment."))
            return
        }

        lastRemovedShipment = getScannedOrderToRemove(barcode)
        if (lastRemovedShipment != null) {
            lastRemovedPosition = scannedShipments.indexOf(lastRemovedShipment!!)
            scannedShipments.remove(lastRemovedShipment!!)
            scannedShipmentsView?.refereshList()

            getBinAndCount()
            scannedShipmentsView?.showSnackBar("Removed Order ${barcode}")
        } else {
            scannedShipmentsView?.onFailure(Throwable("No Shipment Found"))
        }
    }

    private fun getScannedOrderToRemove(refId: String): ScannedOrder? {
        lastRemovedPosition = -1
        lastRemovedShipment = null
        for (shipment in scannedShipments)
            if (shipment.packageId == refId || shipment.lbn == refId || shipment.referenceId == refId) {
                lastRemovedShipment = shipment
            }

        return lastRemovedShipment
    }

    override fun undoRemove() {
        scannedShipments.add(lastRemovedPosition, lastRemovedShipment!!)
        lastRemovedPosition = -1
        lastRemovedShipment = null

        getBinAndCount()
        scannedShipmentsView?.refereshList()
    }

    override fun getUpdatedShipmentList() {
        if (bagDto != null) {
            scannedShipmentsView?.finishActivity()
            return
        }

        scannedPackage?.scannedOrders?.clear()
        scannedPackage?.scannedOrders?.addAll(scannedShipments)
        scannedShipmentsView?.sendUpdatedList(scannedPackage!!, bag_created)
    }

}