package com.goflash.dispatch.features.receiving.presenter.impl

import android.content.Context
import android.content.Intent
import android.util.Log
import co.uk.rushorm.core.RushCore
import co.uk.rushorm.core.RushSearch
import com.goflash.dispatch.data.VehicleDetails
import com.goflash.dispatch.di.interactor.SortationApiInteractor
import com.goflash.dispatch.features.receiving.presenter.SummaryPresenter
import com.goflash.dispatch.features.receiving.view.SummaryView
import com.goflash.dispatch.model.BagDetails
import com.goflash.dispatch.model.Error
import com.goflash.dispatch.model.ReceivingRequest

import com.goflash.dispatch.util.TRIPID
import com.goflash.dispatch.util.VEHICLEID
import com.goflash.dispatch.util.gson
import retrofit2.adapter.rxjava.HttpException
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription

/**
 *Created by Ravi on 2019-09-08.
 */
class SummaryPresenterImpl(val sortationApiInteractor: SortationApiInteractor) : SummaryPresenter {

    private var view: SummaryView? = null
    private var compositeSubscription: CompositeSubscription? = null

    private var vehicleId: String? = null
    private var tripId: String? = null
    private var vehicleDetails = mutableListOf<VehicleDetails>()

    private var tripCompleted = true

    override fun onAttachView(context: Context, view: SummaryView) {
        this.view = view
        compositeSubscription = CompositeSubscription()
    }

    override fun onDetachView() {
        if (view != null)
            view = null

        compositeSubscription!!.unsubscribe()
        compositeSubscription = null
    }

    override fun onTaskResume() {
        vehicleDetails = if (vehicleId != null)
            RushSearch().whereEqual(VEHICLEID, vehicleId).find(VehicleDetails::class.java)
        else
            RushSearch().whereEqual(TRIPID, tripId).find(VehicleDetails::class.java)
        //Log.d("bagDetails", "$vehicleDetails")

        vehicleDetails.removeAll { it.returnReason == "added" }


        view?.updateViews(vehicleDetails)

    }

    override fun onCompleteTask() {

        if (vehicleDetails.any { !it.canBePicked && !it.isScanned }) {
            view?.enableButton()
            return
        }

        view?.onShowProgress()

        vehicleDetails = if (vehicleId != null)
            RushSearch().whereEqual(VEHICLEID, vehicleId).find(VehicleDetails::class.java)
        else
            RushSearch().whereEqual(TRIPID, tripId).find(VehicleDetails::class.java)

        val req = bagDetails()
        if (req.bagRemoved.isEmpty() && req.bagAdded.isEmpty()) {
            RushCore.getInstance().clearDatabase()
            view?.takeToReceivingActivity()
        } else
            compositeSubscription?.add(
                sortationApiInteractor.receivingComplete(req)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        RushCore.getInstance().clearDatabase()
                        view?.onSuccess()
                    }, { error ->

                       httpErrorMessage(error)
                    })
            )
    }

    private fun httpErrorMessage(error: Throwable) {

        if(error is HttpException) {
            val res = error.response()
            val code = res.code()
            if (code == 406) {
                val response = res.errorBody()

                val body = response?.string()
                if (body != null) {

                    val e = gson.fromJson(body, Error::class.java)
                    if (e.message.toLowerCase().contains("completed"))
                        view?.showErrorAndRedirect(e.message)
                }
            } else {
                view?.onFailure(error)
            }
        }else
            view?.onFailure(error)

    }

    private fun bagDetails(): ReceivingRequest {
        val bagDetails = mutableListOf<BagDetails>()

        vehicleDetails.removeAll { it.returnReason == "added" }

        vehicleDetails.forEach {
            bagDetails.add(BagDetails(it.bagId, it.returnReason))
        }

        // val tripId = RushSearch().whereEqual(VEHICLEID, vehicleId).findSingle(ReceivingDto::class.java).tripId
        val req = ReceivingRequest(
            tripId = tripId,
            vehicleSealId = vehicleId,
            tripCompleted = true,
            bagRemoved = bagDetails
        )
        //Log.d("RECEIVEBAG", "Req - " + req)
        return req

    }

    override fun onIntent(intent: Intent) {
        vehicleId = intent.getStringExtra(VEHICLEID)
        tripId = intent.getStringExtra(TRIPID)
    }
}