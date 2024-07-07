package com.goflash.dispatch.features.dispatch.presenter.impl

import android.content.Context
import com.goflash.dispatch.data.ConsolidatedManifestRequest
import com.goflash.dispatch.di.interactor.SortationApiInteractor
import com.goflash.dispatch.features.dispatch.presenter.RunsheetListPresenter
import com.goflash.dispatch.features.dispatch.view.RunsheetListView
import com.goflash.dispatch.util.getCurrentDate
import com.goflash.dispatch.util.getDateFromCurrentDate
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription

class RunsheetListPresenterImpl(private val sortationApiInteractor: SortationApiInteractor): RunsheetListPresenter  {

    private var compositeSubscription: CompositeSubscription? = null
    private var mView: RunsheetListView? = null


    override fun onAttachView(context: Context, view: RunsheetListView) {
        this.mView = view;
        compositeSubscription = CompositeSubscription()

        getRunsheetList()
    }

    override fun onDetachView() {
        if (this.mView == null)
            return
        mView = null
        compositeSubscription?.unsubscribe()
        compositeSubscription = null
    }

    override fun getRunsheetList() {

        compositeSubscription?.add(
            sortationApiInteractor.getConsolidatedManifest(ConsolidatedManifestRequest(startDate = getDateFromCurrentDate(7), endDate = getCurrentDate()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({list ->
                    list?.let {
                        mView?.onRunsheetsFetched(it)
                    }

                }, { error ->
                    mView?.onFailure(error)
                })
        )
    }
}