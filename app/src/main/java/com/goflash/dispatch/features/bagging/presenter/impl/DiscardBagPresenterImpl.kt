package com.goflash.dispatch.features.bagging.presenter.impl

import android.content.Context
import android.content.Intent
import com.goflash.dispatch.app_constants.bag_id
import com.goflash.dispatch.data.BagDTO
import com.goflash.dispatch.di.interactor.SortationApiInteractor
import com.goflash.dispatch.features.bagging.presenter.DiscardBagPresenter
import com.goflash.dispatch.features.bagging.view.DiscardBagView
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription

class DiscardBagPresenterImpl (private val sortationApiInteractor: SortationApiInteractor): DiscardBagPresenter {

    private val TAG = DiscardBagPresenterImpl::class.java.name

    private var discardBagView : DiscardBagView? = null
    private var compositeSubscription : CompositeSubscription? = null

    private var bagDto : BagDTO? = null

    private var bagId : String = ""

    override fun onAttachView(context: Context, view: DiscardBagView) {
        this.discardBagView =view
        compositeSubscription = CompositeSubscription()
    }

    override fun onDetachView() {
        if(discardBagView == null)
            return
        discardBagView = null
        compositeSubscription?.unsubscribe()
        compositeSubscription = null
    }

    override fun sendIntent(intent: Intent) {
        bagId = intent.getStringExtra(bag_id)?:""
        getBagDetail(bagId)
    }

    fun getBagDetail(bagId : String){
        compositeSubscription?.add(sortationApiInteractor.getBagDetail(bagId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({bag ->
                    bagDto = bag
                    if(bagDto != null)
                        discardBagView?.setShipmentCount(bagDto!!.shipmentList.size.toString())

                },{error ->
                    discardBagView?.onFailure(error)
                }))
    }

    override fun onBarcodeScanned(barcode: String) {
        if(barcode == bagDto?.bagId)
            discardBagView?.enableDiscardBtn()
        else
            discardBagView?.onFailure(Throwable("Invalid Barcode"))
    }

    override fun discardBag() {
        compositeSubscription?.add(sortationApiInteractor.discardBag(bagId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    discardBagView?.onSuccess(it.string())

                },{error ->
                    discardBagView?.onFailure(error)
                }))
    }

    override fun getShipmentList() {
        discardBagView?.goToShipmentsActivity(bagDto!!)
    }


}