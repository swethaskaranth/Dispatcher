package com.goflash.dispatch.features.receiving.presenter.impl

import android.content.Context
import android.content.Intent
import android.util.Log
import co.uk.rushorm.core.RushCore
import co.uk.rushorm.core.RushSearch
import com.goflash.dispatch.app_constants.seal_required
import com.goflash.dispatch.data.ReceivingDto
import com.goflash.dispatch.data.VehicleDetails
import com.goflash.dispatch.di.interactor.SortationApiInteractor
import com.goflash.dispatch.features.receiving.presenter.CompletePresenter
import com.goflash.dispatch.features.receiving.view.CompleteScanView
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
import java.text.SimpleDateFormat
import java.util.*

/**
 *Created by Ravi on 2019-09-08.
 */
class CompletePresenterImpl(val sortationApiInteractor: SortationApiInteractor) :
    CompletePresenter {

    private var view: CompleteScanView? = null
    private var compositeSubscription: CompositeSubscription? = null

    private var vehicleId: String? = null
    private var tripId: String? = null
    private var barcodeScanId: String? = null
    private var vehicleDetails = mutableListOf<VehicleDetails>()

    private var sealreuired = true

    override fun onAttachView(context: Context, view: CompleteScanView) {
        this.view = view
        compositeSubscription = CompositeSubscription()
    }

    override fun onDetachView() {
        if (view != null)
            view = null

        compositeSubscription?.unsubscribe()
        compositeSubscription = null

    }

    override fun onBagScanned(barcode: String) {
        if (sealreuired) {
            barcodeScanId = barcode
            verifyVehicleSeal()
        }
    }

    override fun onTaskResume() {
        vehicleDetails = if (vehicleId != null)
            RushSearch().whereEqual(VEHICLEID, vehicleId).find(VehicleDetails::class.java)
        else
            RushSearch().whereEqual(TRIPID, tripId).find(VehicleDetails::class.java)

        //Log.d("bagDetails", "$vehicleDetails")


    }

    override fun onIntent(intent: Intent) {
        vehicleId = intent.getStringExtra(VEHICLEID)
        tripId = intent.getStringExtra(TRIPID)

        sealreuired = vehicleId != null


        if (!sealreuired) {
            val trip =
                RushSearch().whereEqual("tripId", tripId!!).findSingle(ReceivingDto::class.java)
            view?.disableScanner(tripId!!, trip.agentName, getTimeFromISODate(trip.createdOn))
        }
    }


    override fun getSealrequired(): Boolean {
        return sealreuired
    }


    private fun getTimeFromISODate(dateStr: String): String {

        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        val cal = Calendar.getInstance()
        // format.timeZone = TimeZone.getTimeZone("IST")
        cal.time = format.parse(dateStr)


        val dateFormat = SimpleDateFormat("hh:mm a")
        val dateforrow = dateFormat.format(cal.time)

        return dateforrow

    }

    /**
     * Scan vehicle seal and call server
     */
    override fun verifyVehicleSeal() {

        view?.onShowProgress()
        compositeSubscription?.add(sortationApiInteractor.receivingComplete(bagDetails())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                RushCore.getInstance().clearDatabase()
                view?.
                onSuccess()
            },{error ->
               httpErrorMessage(error)
            }))
    }

    private fun httpErrorMessage(error: Throwable) {

        val res = (error as HttpException).response()
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

    }

    private fun bagDetails(): ReceivingRequest {
        val bagDetails = mutableListOf<BagDetails>()

        vehicleDetails.filter { it.canBePicked || (!it.canBePicked && it.isScanned) }.forEach {
            bagDetails.add(BagDetails(it.bagId, it.returnReason))
        }

        //  val tripId = RushSearch().whereEqual("vehicleId", vehicleId).findSingle(ReceivingDto::class.java).tripId

        return ReceivingRequest(tripId = tripId.toString(),
            vehicleSealId = barcodeScanId,
            tripCompleted = false,
            bagRemoved = bagDetails.filter { it.reason != "added" }.toMutableList(),
            bagAdded = bagDetails.filter { it.reason == "added" }.toMutableList()
        )
    }
}