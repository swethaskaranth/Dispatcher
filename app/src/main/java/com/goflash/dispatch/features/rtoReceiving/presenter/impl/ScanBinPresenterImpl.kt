package com.goflash.dispatch.features.rtoReceiving.presenter.impl

import android.content.Context
import com.goflash.dispatch.data.PackageDto
import com.goflash.dispatch.di.interactor.SortationApiInteractor
import com.goflash.dispatch.features.rtoReceiving.presenter.ScanBinPresenter
import com.goflash.dispatch.features.rtoReceiving.view.ScanBinView
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription

class ScanBinPresenterImpl(val sortationApiInteractor: SortationApiInteractor) : ScanBinPresenter {

    private var mView: ScanBinView? = null
    private var compositeSubscription: CompositeSubscription? = null

    override fun onAttach(context: Context, view: ScanBinView) {
        this.mView = view
        compositeSubscription = CompositeSubscription()
    }

    override fun onDetach() {
        if (mView == null)
            return
        mView = null
        compositeSubscription?.unsubscribe()
    }

    override fun onBinScan(barcode: String, packageDto: PackageDto) {
        compositeSubscription?.add(sortationApiInteractor.updateBin(packageDto)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                mView?.onSuccessBinScan()
            }, { error ->
                mView?.onFailure(error)
            }))
    }
}