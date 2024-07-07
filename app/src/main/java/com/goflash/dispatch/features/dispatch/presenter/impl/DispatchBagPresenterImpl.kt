package com.goflash.dispatch.features.dispatch.presenter.impl

import android.content.Context
import co.uk.rushorm.core.RushCore
import co.uk.rushorm.core.RushSearch
import com.goflash.dispatch.data.BagDTO
import com.goflash.dispatch.data.PackageDto
import com.goflash.dispatch.di.interactor.SortationApiInteractor
import com.goflash.dispatch.features.bagging.presenter.impl.DiscardBagPresenterImpl
import com.goflash.dispatch.features.dispatch.presenter.DispatchBagPresenter
import com.goflash.dispatch.features.dispatch.view.DispatchBagView
import com.goflash.dispatch.model.CommonRequest
import com.goflash.dispatch.type.ShipmentType
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription

class DispatchBagPresenterImpl(private val sortationApiInteractor: SortationApiInteractor) : DispatchBagPresenter {

    private val TAG = DiscardBagPresenterImpl::class.java.name

    private var dispatchBagView: DispatchBagView? = null

    private var compositeSubscription: CompositeSubscription? = null

    override fun onAttachView(context: Context, view: DispatchBagView) {
        this.dispatchBagView = view
        compositeSubscription = CompositeSubscription()
    }

    override fun onDetachView() {
        if (dispatchBagView == null)
            return
        dispatchBagView = null
        compositeSubscription?.unsubscribe()
        compositeSubscription = null
    }

    override fun onBarcodeScanned(barcode: String) {
        initiateDispatch(barcode)
    }

    override fun getBagCount() {
        val bagDto = RushSearch().find(BagDTO::class.java)
        dispatchBagView?.setBagCount(bagDto.size.toString())
    }

    override fun initiateDispatch(barcode: String) {
        compositeSubscription?.add(sortationApiInteractor.initiateDispatch(barcode)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    if (result?.scannedOrders?.get(0)?.shipmentType?.equals(ShipmentType.BAG.name) == true) {
                        saveBagToLocal(result)
                        getBagCount()
                    } else {
                        dispatchBagView?.onFailure(Throwable("Not a Bag"))
                    }
                }, { error: Throwable? ->
                    dispatchBagView?.onFailure(error)
                }))

    }

    override fun deleteData() {
        RushCore.getInstance().deleteAll(BagDTO::class.java)
    }

    private fun saveBagToLocal(packageDto: PackageDto) {
        val bag = RushSearch().whereEqual("bagId", packageDto.scannedOrders.get(0).referenceId).findSingle(
            BagDTO::class.java)
        if (bag == null) {
            val bagDto = BagDTO()
            bagDto.bagId = packageDto.scannedOrders.get(0).referenceId
            bagDto.save()
        }

    }

    override fun getVehicleSealRequired() {
        val bags = RushSearch().find(BagDTO::class.java)
        val list = mutableListOf<CommonRequest>()
        for(bag in bags)
            list.add(CommonRequest(tripId = 0, referenceId = bag.bagId))

        compositeSubscription?.add(sortationApiInteractor.getVehicleSealRequired(list)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({sealRequired ->
                dispatchBagView?.onSealRequiedFetched(sealRequired.vehicleSealRequired)

            },{error ->
                dispatchBagView?.onFailure(error)
            }))


    }


}