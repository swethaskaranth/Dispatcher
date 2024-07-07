package com.goflash.dispatch.features.receiving.presenter.impl

import android.content.Context
import android.content.Intent
import co.uk.rushorm.core.*
import com.goflash.dispatch.data.BagDTO
import com.goflash.dispatch.data.VehicleDetails
import com.goflash.dispatch.di.interactor.SortationApiInteractor
import com.goflash.dispatch.features.dispatch.presenter.ScannedBagsPresenter
import com.goflash.dispatch.features.bagging.view.BagRowView
import com.goflash.dispatch.features.dispatch.view.ScannedBagsView
import com.goflash.dispatch.features.receiving.presenter.RemovedBagsPresenter
import com.goflash.dispatch.features.receiving.view.RemoveBagsView
import com.goflash.dispatch.util.RETURNREASON
import com.goflash.dispatch.util.TRIPID
import com.goflash.dispatch.util.VEHICLEID
import rx.subscriptions.CompositeSubscription

class RemovedBagsPresenterImpl(private val sortationApiInteractor: SortationApiInteractor) : RemovedBagsPresenter {

    private val TAG = RemovedBagsPresenterImpl::class.java.name

    private var scannedBagsView: RemoveBagsView? = null

    private var compositeSubscription: CompositeSubscription? = null

    private var lastRemovedBag: VehicleDetails? = null

    private var lastRemovedPosition: Int = -1

    private var vehicleId: String? = null
    private var tripId: String? = null

    private var bagList = mutableListOf<VehicleDetails>()

    override fun onAttachView(context: Context, view: RemoveBagsView) {
        this.scannedBagsView = view
        compositeSubscription = CompositeSubscription()
        getBagList()
    }

    override fun onDeatchView() {
        if (scannedBagsView == null)
            return
        scannedBagsView = null
        compositeSubscription?.unsubscribe()
        compositeSubscription = null
    }

    private fun getBagList() {
        bagList = if(vehicleId != null)
            RushSearch().whereEqual(VEHICLEID,vehicleId).and().whereEqual(RETURNREASON, "added").find(VehicleDetails::class.java)
        else
            RushSearch().whereEqual(TRIPID,tripId).and().whereEqual(RETURNREASON, "added").find(VehicleDetails::class.java)

        getBagCount()
    }

    override fun getCount(): Int {
        return bagList.size
    }


    override fun onBindBagdRowView(position: Int, holder: BagRowView) {
        val bag = bagList[position]
        holder.setBagId(bag.bagId)

    }

    override fun onBarcodeScanned(barcode: String) {

        lastRemovedBag = getScannedOrderToRemove(barcode)

        if(lastRemovedBag != null) {
            lastRemovedPosition = bagList.indexOf(lastRemovedBag!!)
           // allBagList.remove(lastRemovedBag!!)
            bagList.remove(lastRemovedBag!!)
            scannedBagsView?.refreshList()
            getBagCount()
            scannedBagsView?.showSnackBar("Bag ID ${lastRemovedBag?.bagId} has been removed from the trip")
        }else{
            scannedBagsView?.onFailure(Throwable("No Bag Found"))
        }
    }

    override fun getUpdatedBagList() {
        val list = if(vehicleId != null)
            RushSearch().whereEqual(VEHICLEID,vehicleId).and().whereEqual(RETURNREASON, "added").find(VehicleDetails::class.java)
        else
            RushSearch().whereEqual(TRIPID,tripId).and().whereEqual(RETURNREASON, "added").find(VehicleDetails::class.java)


        RushCore.getInstance().delete(list)

        for (bag in bagList)
            bag.save()

        scannedBagsView?.finishActivity(vehicleId, tripId)

    }

    override fun undoRemove() {
        bagList.add(lastRemovedPosition, lastRemovedBag!!)
        lastRemovedPosition = -1
        lastRemovedBag = null

        getBagCount()
        scannedBagsView?.refreshList()
    }

    private fun getScannedOrderToRemove(refId: String): VehicleDetails? {
        lastRemovedPosition = -1
        lastRemovedBag = null
        for (bag in bagList)
            if (bag.bagId == refId) {
                lastRemovedBag = bag
            }

        return lastRemovedBag
    }

    private fun getBagCount() {
        scannedBagsView?.setCount(bagList.size)
    }

    override fun onIntent(intent: Intent) {
        vehicleId = intent.getStringExtra(VEHICLEID)
        tripId = intent.getStringExtra(TRIPID)
    }


}