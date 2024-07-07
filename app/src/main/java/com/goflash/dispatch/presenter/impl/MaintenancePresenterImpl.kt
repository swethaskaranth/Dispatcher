package com.goflash.dispatch.presenter.impl

import android.content.Context
import com.goflash.dispatch.di.interactor.SortationApiInteractor
import com.goflash.dispatch.presenter.MaintenancePresenter
import com.goflash.dispatch.presenter.views.MaintenanceView
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription

class MaintenancePresenterImpl(private val sortationApiInteractor: SortationApiInteractor) :
    MaintenancePresenter {

    private var mView: MaintenanceView? = null

    private var compositeSubscription: CompositeSubscription? = null

    override fun onAttachView(context: Context, view: MaintenanceView) {
        this.mView = view
        this.compositeSubscription = CompositeSubscription()
    }

    override fun onDetachView() {
        if (mView == null)
            return
        mView = null
        compositeSubscription?.unsubscribe()
        compositeSubscription = null
    }

    override fun checkHealthStatus() {
        compositeSubscription?.add(
            sortationApiInteractor.checkHealthStatus()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ maintenanceMode ->
                    mView?.onMaintenanceModeFetched(maintenanceMode)
                }, {
                    mView?.onFailure(it)
                })
        )
    }


}