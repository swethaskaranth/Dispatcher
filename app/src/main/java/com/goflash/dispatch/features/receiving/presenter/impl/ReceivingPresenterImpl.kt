package com.goflash.dispatch.features.receiving.presenter.impl

import android.content.Context
import com.goflash.dispatch.data.ReceivingDto
import com.goflash.dispatch.di.interactor.SortationApiInteractor
import com.goflash.dispatch.features.receiving.presenter.ReceivingPresenter
import com.goflash.dispatch.features.receiving.view.ReceivingView
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription

/**
 *Created by Ravi on 2019-09-08.
 */
class ReceivingPresenterImpl(val sortationApiInteractor: SortationApiInteractor) :
    ReceivingPresenter {

    private var view: ReceivingView? = null
    private var compositeSubscription: CompositeSubscription? = null

    private var receivingDto: MutableList<ReceivingDto>? = null

    override fun onAttachView(context: Context, view: ReceivingView) {
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

    override fun getAllTasks() {
        view?.onShowProgress()

        compositeSubscription?.add(sortationApiInteractor.getExpectedTrips()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ it ->
                receivingDto = it
                receivingDto?.let { list ->
                    view?.onSuccess(list)
                }

            }, { error ->
                view?.onFailure(error)
            })
        )
    }

    override fun searchByVehicleSeal(str: String) {
        val filteredList = mutableListOf<ReceivingDto>()
        receivingDto?.filter {
            it.vehicleId?.lowercase()?.contains(str.lowercase()) == true
        }?.toMutableList()?.let { filteredList.addAll(it) }
        view?.onSuccess(filteredList)

    }

    override fun clearFilter() {
        receivingDto?.let { list ->
            view?.onSuccess(list)
        }
    }


}