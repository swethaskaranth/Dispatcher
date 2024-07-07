package com.goflash.dispatch.features.lastmile.tripCreation.presenter.impl

import android.content.Context
import co.uk.rushorm.core.RushSearch
import com.goflash.dispatch.data.AckSlipDto
import com.goflash.dispatch.data.TripSettlementDTO
import com.goflash.dispatch.data.TripSettlementParams
import com.goflash.dispatch.data.TripSettlementReceiver
import com.goflash.dispatch.di.interactor.SortationApiInteractor
import com.goflash.dispatch.features.lastmile.tripCreation.presenter.CompletedPresenter
import com.goflash.dispatch.features.lastmile.tripCreation.view.CompletedView
import com.goflash.dispatch.model.ActiveTrips
import com.goflash.dispatch.type.BagStatus
import com.goflash.dispatch.util.PreferenceHelper
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription

class CompletedPresenterImpl(private val sortationApiInteractor: SortationApiInteractor): CompletedPresenter {

    private var view: CompletedView? = null

    private var compositeSubscription: CompositeSubscription? = null

    private var apiCalled = false

    override fun onAttachView(context: Context, view: CompletedView) {
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

     override fun getCompletedTrips() {
         if(!apiCalled) {
             apiCalled = true
             compositeSubscription?.add(sortationApiInteractor.getParticularTrips(request())
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

    override fun getReconStartedTrip(tripId: Long) {
        compositeSubscription?.add(sortationApiInteractor.getTripSettlementDetails(tripId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({tripSettlement ->
                view?.onSuccessSettlement(tripSettlement, tripId)
            }, {
                view?.onFailure(it)
            }))
    }

    override fun saveTripSettlementData() {

    }

    private fun request(): ActiveTrips {
        // add RECON_STARTED when api updated
        return ActiveTrips("${BagStatus.COMPLETED.name},${BagStatus.RECON_STARTED.name}",
            PreferenceHelper.startDate, PreferenceHelper.endDate, agentName = PreferenceHelper.agentName,excludedTrips = mutableListOf())
    }

    override fun getUndeliveredData(tripId: String): TripSettlementDTO {
        return RushSearch().whereEqual("tripId", tripId).findSingle(TripSettlementDTO::class.java)
    }

}