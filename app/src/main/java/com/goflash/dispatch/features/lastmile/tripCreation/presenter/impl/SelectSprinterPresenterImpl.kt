package com.goflash.dispatch.features.lastmile.tripCreation.presenter.impl

import android.content.Context
import com.goflash.dispatch.data.SprinterList
import com.goflash.dispatch.di.interactor.SortationApiInteractor
import com.goflash.dispatch.features.lastmile.tripCreation.presenter.SelectSprinterPresenter
import com.goflash.dispatch.features.lastmile.tripCreation.view.SelectSprinterView
import com.goflash.dispatch.model.AssignSprinter
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription

class SelectSprinterPresenterImpl(private val sortationApiInteractor: SortationApiInteractor) :
    SelectSprinterPresenter {

    private var selectSprinterView: SelectSprinterView? = null
    private var compositeSubscription: CompositeSubscription? = null

    override fun onAttachView(context: Context, view: SelectSprinterView) {
        this.selectSprinterView = view
        compositeSubscription = CompositeSubscription()

        getSprinters()

    }

    override fun onDetachView() {
        if (selectSprinterView == null)
            return
        selectSprinterView = null
        compositeSubscription?.unsubscribe()
        compositeSubscription = null
    }

    private fun getSprinters() {
        compositeSubscription?.add(sortationApiInteractor.getSprinters()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({ list ->
                selectSprinterView?.onSprintersFetched(list.sortedBy { it.name })
            }, { error ->
                selectSprinterView?.onFailure(error)

            })
        )
    }

    override fun createTrip(sprinter: SprinterList) {
        compositeSubscription?.add(sortationApiInteractor.createManualTrip(sprinter.id)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({trip ->
                selectSprinterView?.onCreateTripSuccess(trip.idForTrip,trip.agentName)

            }, { error ->
                selectSprinterView?.onFailure(error)

            })
        )
    }

    override fun assignSprinter(tripId: Long, sprinter: SprinterList) {
        compositeSubscription?.add(sortationApiInteractor.assignSprinterToTrip(AssignSprinter(tripId,sprinter.id))
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({_ ->
                selectSprinterView?.onAssignSuccess()

            }, { error ->
                selectSprinterView?.onFailure(error)

            })
        )
    }
}