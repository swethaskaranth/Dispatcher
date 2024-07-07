package com.goflash.dispatch.features.dispatch.presenter.impl

import android.content.Context
import android.content.Intent
import co.uk.rushorm.core.RushCore
import co.uk.rushorm.core.RushSearch
import com.goflash.dispatch.app_constants.*
import com.goflash.dispatch.data.BagDTO
import com.goflash.dispatch.data.BagTripDTO
import com.goflash.dispatch.data.ConsolidatedManifestRequest
import com.goflash.dispatch.data.Invoice
import com.goflash.dispatch.data.MidMileDispatchedRunsheet
import com.goflash.dispatch.data.Sprinter
import com.goflash.dispatch.di.interactor.SortationApiInteractor
import com.goflash.dispatch.features.dispatch.presenter.DispatchVehiclePresenter
import com.goflash.dispatch.features.dispatch.ui.interfaces.InvoiceListController
import com.goflash.dispatch.features.dispatch.view.DispatchVehicleView
import com.goflash.dispatch.features.dispatch.view.InvoiceRowView
import com.goflash.dispatch.model.InvoiceDetailRequest
import com.goflash.dispatch.util.getTimeFromISODate
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription
import java.text.SimpleDateFormat
import java.util.*

class DispatchVehiclePresenterImpl (private val sortationApiInteractor: SortationApiInteractor) :
    DispatchVehiclePresenter, InvoiceListController {

    private val TAG = DispatchVehiclePresenterImpl::class.java.name

    private var dispatchVehicleView: DispatchVehicleView? = null

    private var compositeSubscription: CompositeSubscription? = null

    private var sprinterSelected: Sprinter? = null

    private var transportMode: String? = null

    private var tripId: String? = null

    private var invoiceRequired = false

    private var invoice: Invoice? = null

    private var ewayBillPrint = false

    private var invoice_click_count = 0

    private var sealIdRequired = true

    private val invoiceList: ArrayList<Invoice> = ArrayList()

    override fun onAttachView(context: Context, view: DispatchVehicleView) {
        this.dispatchVehicleView = view
        compositeSubscription = CompositeSubscription()
    }

    override fun sendIntent(intent: Intent) {
        sealIdRequired = intent.getBooleanExtra(seal_required, true)
        invoiceRequired = intent.getBooleanExtra(invoice_required, false)
        if (sealIdRequired && intent.hasExtra(sprinter)) {

            sprinterSelected = intent.getParcelableExtra<Sprinter>(sprinter) as Sprinter
            transportMode = intent.getStringExtra(transMode)
            dispatchVehicleView?.setupProceedButton(invoiceRequired, false)
            dispatchVehicleView?.enableordisableScanner(true, "", "", "")
        } else {
            tripId = intent.getStringExtra(trip_id)
            dispatchVehicleView?.setupProceedButton(invoiceRequired, true)
            if(invoiceRequired)
                getInvoiceList()
            //getConsolidatedManifest()
            dispatchVehicleView?.enableordisableScanner(
                false, tripId!!, intent.getStringExtra(
                    sprinter_name
                )?:"", getCurrentDateAndTime()
            )
        }

    }

    private fun getCurrentDateAndTime(): String {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy | hh:mm a")
        val cal = Calendar.getInstance()
        return dateFormat.format(cal.time)
    }

    override fun onDetachView() {
        if (dispatchVehicleView == null)
            return
        dispatchVehicleView = null
        compositeSubscription?.unsubscribe()
        compositeSubscription = null
    }

    override fun onBarcodeScanned(barcode: String) {
        if (sealIdRequired) {
            compositeSubscription?.add(
                sortationApiInteractor.createBagTrips(getBagDTO(barcode))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        RushCore.getInstance().clearDatabase()
                        tripId = it.tripId
                        invoiceRequired = (it.invoiceRequired != null && it.invoiceRequired)
                        dispatchVehicleView?.onSuccess("Trip Created Successfully", invoiceRequired)
                        dispatchVehicleView?.enableordisableScanner(
                            false,
                            tripId!!,
                            sprinterSelected?.name ?: "",
                            getCurrentDateAndTime()
                        )
                    }, { error ->
                        dispatchVehicleView?.onFailure(error)
                        dispatchVehicleView?.setBarcodeScanned(false)
                    })
            )
        }
    }

    private fun getBagDTO(barcode: String): BagTripDTO {
        val bags = RushSearch().find(BagDTO::class.java)
        if(transportMode != "ROAD")
            sprinterSelected?.vehicleNumber = null
        return BagTripDTO(null, barcode, sprinterSelected!!, bags, transportMode)

    }

    override fun getInvoiceList() {
        compositeSubscription?.add(
            sortationApiInteractor.getInvoiceList(tripId = tripId?.toLong()?:0L)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    invoiceList.clear()
                    invoiceList.addAll(it)
                    dispatchVehicleView?.onListFetched(invoiceList)

                }, { error ->
                    dispatchVehicleView?.onFailure(error)
                })
        )
    }

    override fun getInvoiceUrl() {
        if (invoiceRequired)
            compositeSubscription?.add(
                sortationApiInteractor.getInvoiceUrl(InvoiceDetailRequest(tripId = tripId?.toLong()?:0L,invoiceId = null))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        invoice = it
                        if (invoice != null)
                            dispatchVehicleView?.printFromUrl("Consolidated Invoice PDF",invoice!!.invoiceUrl!!)
                        else
                            dispatchVehicleView?.onFailure(Throwable("Invoice creation failed"))
                    }, { error ->
                        dispatchVehicleView?.onFailure(error)
                    })
            )
        else
            dispatchVehicleView?.onPrintSuccess()
    }



    override fun onPrintFinished() {
        if (!ewayBillPrint) {
            if (invoice?.wayWillNumberUrl != null && invoice?.wayWillNumberUrl!!.isNotEmpty()) {
                dispatchVehicleView?.printFromUrl("Eway Bill PDF",invoice?.wayWillNumberUrl!!)
                ewayBillPrint = true
            }
        } else {
            ewayBillPrint = false
            invoice = null
        }

    }

    override fun getCount(): Int {
        return invoiceList.size

    }

    override fun onBindInvoiceRowView(position: Int, invoiceRowView: InvoiceRowView) {
        val invoice =
            invoiceList[position]


        invoiceRowView.setInvoiceId(invoice.invoiceId)
        invoiceRowView.setTime(getTimeFromISODate(invoice.createdOn))

        invoiceRowView.hideEwayBill()

        invoiceRowView.setOnClickListeners()
    }

    override fun onPrintInvoiceClicked(position: Int) {
        val invoice = invoiceList[position]
        if (invoice.invoiceUrl != null)
            dispatchVehicleView?.printFromUrl("Consolidated Invoice PDF", invoice.invoiceUrl)
        else {
            getInvoice(invoice)
        }
    }

    private fun getInvoice(inv: Invoice) {
        compositeSubscription?.add(sortationApiInteractor.getInvoiceUrlv2(inv.invoiceId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                invoice = it
                if (it.invoiceUrl != null)
                    dispatchVehicleView?.printFromUrl("Consolidated Invoice PDF",it.invoiceUrl)
            }, {
                dispatchVehicleView?.onFailure(it)

            })
        )
    }

    override fun onEwayBillClicked(position: Int) {
        val invoice = invoiceList[position]
        dispatchVehicleView?.printFromUrl("Eway Bill PDF",invoice.wayWillNumberUrl!!)
    }

    override fun getConsolidatedManifest() {
        compositeSubscription?.add(
            sortationApiInteractor.getConsolidatedManifest(ConsolidatedManifestRequest( tripId = tripId?.toLong()?:0L))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if(it.isNullOrEmpty())
                        dispatchVehicleView?.onFailure(Throwable("Manifest generation in progress. Please wait."))
                    else{
                        it[0].runsheetUrl?.let { it1 ->
                            dispatchVehicleView?.printFromUrl("Consolidated Manifest",
                                it1
                            )
                        }
                    }

                }, { error ->
                    dispatchVehicleView?.onFailure(error)
                })
        )
    }


}