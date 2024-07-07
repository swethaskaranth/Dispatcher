package com.goflash.dispatch.features.lastmile.tripCreation.presenter.impl

import android.content.Context
import co.uk.rushorm.core.RushSearch
import com.goflash.dispatch.data.SprinterForZone
import com.goflash.dispatch.di.interactor.SortationApiInteractor
import com.goflash.dispatch.features.lastmile.tripCreation.presenter.ZoneSprinterPresenter
import com.goflash.dispatch.features.lastmile.tripCreation.view.ZoneSprinterView
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription

class ZoneSprinterPresenterImpl (private val sortationApiInteractor: SortationApiInteractor) : ZoneSprinterPresenter{

    private var mView : ZoneSprinterView? = null

    private var compositeSubscription : CompositeSubscription? = null

    override fun onAttachView(context: Context, zoneSprinterView: ZoneSprinterView) {
        this.mView = zoneSprinterView
        this.compositeSubscription = CompositeSubscription()

    }

    override fun getSprinters(selectedSprinter : List<SprinterForZone>) {
        compositeSubscription?.add(sortationApiInteractor.getSprinters()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({ list ->
                val sprinterList = mutableListOf<SprinterForZone>()
                for(sprinter in list){
                    val zoneSprinter = SprinterForZone()
                    zoneSprinter.sprinterId = sprinter.id
                    zoneSprinter.name = sprinter.name
                    zoneSprinter.isRestricted = sprinter.restricted
                    zoneSprinter.restrictionReason = sprinter.restrictionReason
                    sprinterList.add(zoneSprinter)
                }
                val savedSprinters = RushSearch().find(SprinterForZone::class.java)
                savedSprinters.removeAll(selectedSprinter)
                sprinterList.removeAll(savedSprinters)
                sprinterList.addAll(savedSprinters)
                mView?.onSprintersFetched(sprinterList)
            }, { error ->
                mView?.onFailure(error)

            })
        )
    }

    override fun onDetachView() {
        if(mView == null)
            return
        mView = null
        compositeSubscription?.unsubscribe()
        compositeSubscription = null
    }
}