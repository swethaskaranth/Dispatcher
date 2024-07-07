package com.goflash.dispatch.features.lastmile.tripCreation.presenter.impl

import android.content.Context
import com.goflash.dispatch.di.interactor.SortationApiInteractor
import com.goflash.dispatch.features.lastmile.tripCreation.presenter.FilterPresenter
import com.goflash.dispatch.features.lastmile.tripCreation.view.FilterView
import rx.subscriptions.CompositeSubscription

class FilterPresenterImpl (private val sortationApiInteractor: SortationApiInteractor) : FilterPresenter {


    private var mView: FilterView? = null
    private var compositeSubscription: CompositeSubscription? = null

    override fun onAttach(context: Context, view: FilterView) {
        this.mView = view
        compositeSubscription = CompositeSubscription()
    }

    override fun onDetach() {
        if (mView == null)
            return
        mView = null
        compositeSubscription?.unsubscribe()
        compositeSubscription = null
    }

}