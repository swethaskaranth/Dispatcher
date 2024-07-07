package com.goflash.dispatch.features.cash.presenter.impl

import android.content.Context
import com.goflash.dispatch.api_services.SessionService
import com.goflash.dispatch.di.interactor.SortationApiInteractor
import com.goflash.dispatch.features.cash.presenter.CashPresenter
import com.goflash.dispatch.features.cash.view.CashView
import com.goflash.dispatch.util.PreferenceHelper
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription

/**
 *Created by Ravi on 01/10/20.
 */
class CashPresenterImpl(private val sortationApiInteractor: SortationApiInteractor) : CashPresenter {

    private var mView: CashView? = null

    private var compositeSubscription: CompositeSubscription? = null

    override fun onAttach(context: Context, view: CashView) {
        mView = view
        compositeSubscription = CompositeSubscription()
    }

    override fun onDeAttach() {
        if (mView == null)
            return
        mView = null
        compositeSubscription?.unsubscribe()
        compositeSubscription = null
    }

    override fun getAllCash(page: Int, size: Int) {
        compositeSubscription?.add(sortationApiInteractor.getCashClosingDetails(page,size,PreferenceHelper.assignedAssetId.toInt())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ it ->
                mView?.onSuccess(it)
            }, { error ->
                mView?.onFailure(error)

            })
        )
    }
}