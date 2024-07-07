package com.goflash.dispatch.features.lastmile.tripCreation.presenter.impl

import android.content.Context
import com.goflash.dispatch.di.interactor.SortationApiInteractor
import com.goflash.dispatch.features.lastmile.tripCreation.presenter.ReconFinishPresenter
import com.goflash.dispatch.features.lastmile.tripCreation.view.ReconFinishView
import com.goflash.dispatch.model.ActiveTrips
import com.goflash.dispatch.type.BagStatus
import com.goflash.dispatch.util.PreferenceHelper
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription

class ReconFinishPresenterImpl(private val sortationApiInteractor: SortationApiInteractor) :
    ReconFinishPresenter {

    private var view: ReconFinishView? = null

    private var compositeSubscription: CompositeSubscription? = null

    private var apiCalled = false

    override fun onAttachView(context: Context, view: ReconFinishView) {
        this.view = view
        compositeSubscription = CompositeSubscription()
    }

    override fun onDetachView() {
        if (view == null)
            return
        view = null
        compositeSubscription?.unsubscribe()
        compositeSubscription = null
    }

    override fun getReconFinishTrips() {
        if (!apiCalled) {
            apiCalled = true
            compositeSubscription?.add(
                sortationApiInteractor.getParticularTrips(request())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        view?.onSuccess(it)
                        apiCalled = false
                    }, {
                        view?.onFailure(it)
                        apiCalled = false
                    })
            )
        }
    }

    private fun request(): ActiveTrips {
        return ActiveTrips(
            BagStatus.RECON_FINISHED.name,
            PreferenceHelper.startDate,
            PreferenceHelper.endDate,
            agentName = PreferenceHelper.agentName,
            excludedTrips = mutableListOf()
        )
    }

}