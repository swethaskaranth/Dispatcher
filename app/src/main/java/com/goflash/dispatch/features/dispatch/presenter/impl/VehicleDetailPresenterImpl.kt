package com.goflash.dispatch.features.dispatch.presenter.impl

import android.content.Context
import android.content.Intent
import co.uk.rushorm.core.RushCore
import co.uk.rushorm.core.RushSearch
import com.goflash.dispatch.app_constants.seal_required
import com.goflash.dispatch.data.BagDTO
import com.goflash.dispatch.data.BagTripDTO
import com.goflash.dispatch.data.Sprinter
import com.goflash.dispatch.di.interactor.SortationApiInteractor
import com.goflash.dispatch.features.dispatch.presenter.VehicleDetailPresenter
import com.goflash.dispatch.features.dispatch.view.VehicleDetailView
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription
import java.util.regex.Pattern

class VehicleDetailPresenterImpl(private val sortationApiInteractor: SortationApiInteractor) :
    VehicleDetailPresenter {

    private val TAG = VehicleDetailPresenterImpl::class.java.name

    private var vehicleDetailView: VehicleDetailView? = null

    private var compositeSubscription: CompositeSubscription? = null

    private var sprinterList = mutableListOf<Sprinter>()

    private var selectedIndex = -1

    private var sealreuired = true

    private var tripId: String? = null

    private var invoiceRequired = false

    override fun onAttachView(context: Context, view: VehicleDetailView) {
        this.vehicleDetailView = view
        compositeSubscription = CompositeSubscription()
    }

    override fun sendIntent(intent: Intent) {
        sealreuired = intent.getBooleanExtra(seal_required, true)

        if (!sealreuired)
            vehicleDetailView?.disableVehicleNumber()

    }


    override fun onDetachView() {
        if (vehicleDetailView == null)
            return
        vehicleDetailView = null
        compositeSubscription?.unsubscribe()
        compositeSubscription = null
    }

    override fun getSprinterList() {
        compositeSubscription?.add(
            sortationApiInteractor.getSprinterList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    sprinterList.addAll(it)
                    vehicleDetailView?.onSprinterListFetched(
                        getSprinterNames(sprinterList),
                        if (sealreuired) "Proceed" else "Dispatch"
                    )
                }, { error ->
                    vehicleDetailView?.onFailure(error)

                })
        )
    }

    private fun getSprinterNames(list: MutableList<Sprinter>): MutableList<String> {
        val names = mutableListOf<String>()
        list.map { it.name }.toCollection(names)

        return names
    }

    override fun onSprinterSelected(sprinter: String) {

        selectedIndex = getSprinterPosition(sprinter)
        if (sealreuired) {
            vehicleDetailView?.setVehicleNumber(sprinterList[selectedIndex].vehicleNumber, false)
            vehicleDetailView?.enableOrDisableVehicleNumber()
        }
        vehicleDetailView?.enableProceedBtn()
    }

    override fun onProceedClicked(transportMode: String?) {
        if (sealreuired)
            getSelectedSprinter()
        else
            dispatchBags()
    }

    private fun getSprinterPosition(name: String): Int {
        for (sprinter in sprinterList)
            if (sprinter.name == name)
                return sprinterList.indexOf(sprinter)

        return -1
    }


    override fun getSelectedSprinter() {
        if (selectedIndex != -1)
            vehicleDetailView?.startVehcileSealScanActivity(sprinterList[selectedIndex])
        else
            vehicleDetailView?.onFailure(Throwable("Please select a Sprinter"))
    }

    private fun dispatchBags() {
        compositeSubscription?.add(
            sortationApiInteractor.createBagTrips(getBagDTO())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    RushCore.getInstance().clearDatabase()
                    tripId = it.tripId
                    invoiceRequired = (it.invoiceRequired != null && it.invoiceRequired)
                    vehicleDetailView?.onSuccess(
                        "Trip Created Successfully",
                        tripId!!,
                        sprinterList[selectedIndex].name,
                        invoiceRequired
                    )
                }, { error ->
                    vehicleDetailView?.onFailure(error)
                })
        )

    }

    private fun getBagDTO(): BagTripDTO {
        val bags = RushSearch().find(BagDTO::class.java)
        val sprinter = sprinterList[selectedIndex]
        sprinter.vehicleNumber = null
        return BagTripDTO(null, null, sprinter, bags,  null)
    }

    override fun validateVehicleNumber(number: String) {
        val pattern =
            Pattern.compile("^(?!DL)[A-Z]{2}[A-Z0-9]+[0-9]{4}\$|[D][L][0-9]{2}[A-Z0-9]+[0-9]{4}")

        if (!pattern.matcher(number).find() || number.isEmpty()) {
            vehicleDetailView?.showInvalidVehicleNumber()
            return
        }

        if (selectedIndex == -1) {
            vehicleDetailView?.onFailure(Throwable("Please select a Sprinter"))
            return
        }

        compositeSubscription?.add(
            sortationApiInteractor.updateVehicleNumber(sprinterList[selectedIndex].id, number)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ success ->
                    sprinterList[selectedIndex].vehicleNumber = number
                    vehicleDetailView?.setVehicleNumber(number, true)
                    vehicleDetailView?.enableProceedBtn()

                }, { error ->
                    vehicleDetailView?.onFailure(error)

                })
        )

    }

    override fun getSelectedSprinterDetails() {

        if (sealreuired)
            vehicleDetailView?.setVehicleNumber(
                if (selectedIndex != -1) sprinterList[selectedIndex].vehicleNumber else null,
                false, false
            )

    }


}