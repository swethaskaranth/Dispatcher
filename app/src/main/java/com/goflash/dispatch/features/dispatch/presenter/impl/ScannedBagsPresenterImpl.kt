package com.goflash.dispatch.features.dispatch.presenter.impl

import android.content.Context
import co.uk.rushorm.core.RushCore
import co.uk.rushorm.core.RushSearch
import com.goflash.dispatch.data.BagDTO
import com.goflash.dispatch.di.interactor.SortationApiInteractor
import com.goflash.dispatch.features.dispatch.presenter.ScannedBagsPresenter
import com.goflash.dispatch.features.bagging.view.BagRowView
import com.goflash.dispatch.features.dispatch.view.ScannedBagsView
import rx.subscriptions.CompositeSubscription

class ScannedBagsPresenterImpl(private val sortationApiInteractor: SortationApiInteractor) : ScannedBagsPresenter {

    private val TAG = ScannedBagsPresenterImpl::class.java.name

    private var scannedBagsView: ScannedBagsView? = null

    private var compositeSubscription: CompositeSubscription? = null

    private var lastRemovedBag: BagDTO? = null

    private var lastRemovedPosition: Int = -1

    private var bagList = mutableListOf<BagDTO>()

    override fun onAttachView(context: Context, view: ScannedBagsView) {
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

    fun getBagList() {
        bagList = RushSearch().find(BagDTO::class.java)
        getBagCount()
    }

    override fun getCount(): Int {
        return bagList.size
    }


    override fun onBindBagdRowView(position: Int, holder: BagRowView) {
        val bag = bagList.get(position)
        holder.setBagId(bag.bagId)

    }

    override fun onBarcodeScanned(barcode: String) {

        lastRemovedBag = getScannedOrderToRemove(barcode)
        if(lastRemovedBag != null) {
            lastRemovedPosition = bagList.indexOf(lastRemovedBag!!)
            bagList.remove(lastRemovedBag!!)
            scannedBagsView?.refreshList()

            getBagCount()
            scannedBagsView?.showSnackBar("Bag ID ${lastRemovedBag?.bagId} has been removed from the trip")
        }else{
            scannedBagsView?.onFailure(Throwable("No Bag Found"))
        }
    }

    override fun getUpdatedBagList(addBag: Boolean) {
        if(!addBag) {
            RushCore.getInstance().deleteAll(BagDTO::class.java)

            for (bag in bagList)
                bag.save()
        }

        scannedBagsView?.finishActivity()

    }

    override fun undoRemove() {
        bagList.add(lastRemovedPosition, lastRemovedBag!!)
        lastRemovedPosition = -1
        lastRemovedBag = null

        getBagCount()
        scannedBagsView?.refreshList()
    }

    private fun getScannedOrderToRemove(refId: String): BagDTO? {
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



}