package com.goflash.dispatch.features.cash.presenter.impl

import android.content.Context
import com.goflash.dispatch.di.interactor.SortationApiInteractor
import com.goflash.dispatch.features.cash.presenter.CashCollectionBreakupPresenter
import com.goflash.dispatch.features.cash.view.CashCollectionBreakupView
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription

class CashCollectionBreakupPresenterImpl(private val sortationApiInteractor: SortationApiInteractor) : CashCollectionBreakupPresenter {

    private var mView: CashCollectionBreakupView? = null

    private var compositeSubscription: CompositeSubscription? = null

    private var context: Context? = null

    private var page = 0

    private var totalPages = 0

    override fun onAttach(context: Context, view: CashCollectionBreakupView) {
        this.context = context
        this.mView = view
        compositeSubscription = CompositeSubscription()

    }

    override fun onDetach() {
        if (mView == null)
            return
        mView = null
        compositeSubscription?.unsubscribe()
    }

    override fun getCashCollectionBreakup(cashClosingId: String?, size: Int, adhoc: Boolean){
        if(adhoc)
            getAdhocBreakup(cashClosingId, size)
        else
        compositeSubscription?.add(
            sortationApiInteractor.getCashCollectionBreakup(cashClosingId,page++,size)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if(it.elements > 0) {
                    totalPages = it.pages
                    mView?.onBreakupFetched(it.content.toMutableList())
                }else
                    mView?.showNoElementsView()

            },{
                mView?.onFailure(it)
            }))
    }

    private fun getAdhocBreakup(cashClosingId: String?, size: Int){
        compositeSubscription?.add(
            sortationApiInteractor.getAdhocCashCollectionBreakup(cashClosingId,page++,size)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if(it.elements > 0) {
                        totalPages = it.pages
                        mView?.onAdhocBreakupFetched(it.content.toMutableList())
                    }else
                        mView?.showNoElementsView()

                },{
                    mView?.onFailure(it)
                }))
    }

    override fun isLastPage(): Boolean {
       return totalPages == page
    }
}