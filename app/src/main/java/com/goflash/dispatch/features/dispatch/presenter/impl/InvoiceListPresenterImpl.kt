package com.goflash.dispatch.features.dispatch.presenter.impl

import android.content.Context
import android.util.Log
import com.goflash.dispatch.data.Invoice
import com.goflash.dispatch.di.interactor.SortationApiInteractor
import com.goflash.dispatch.features.dispatch.presenter.InvoiceListPresenter
import com.goflash.dispatch.features.dispatch.ui.interfaces.InvoiceListController
import com.goflash.dispatch.features.dispatch.view.InvoiceListView
import com.goflash.dispatch.features.dispatch.view.InvoiceRowView
import com.goflash.dispatch.model.InvoiceDetailRequest
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class InvoiceListPresenterImpl(private val sortationApiInteractor: SortationApiInteractor) :
    InvoiceListPresenter, InvoiceListController {

    private val TAG = DispatchPresenterImpl::class.java.simpleName

    private var invoiceView: InvoiceListView? = null

    private var compositeSubscription: CompositeSubscription? = null

    private var date_spinner_list = mutableListOf<String>()

    private val invoiceList: ArrayList<Invoice> = ArrayList()
    private val filteredList: ArrayList<Invoice> = ArrayList()

    private val vehicleList: ArrayList<String> = ArrayList()

    private var filter_applied = false

    private var invoice_number: String = ""

    private val dateList = mutableListOf<String>()

    private var today = true

    private var vehicle: String = ""

    private var from: Date? = null
    private var to: Date? = null


    override fun onAttachView(context: Context, view: InvoiceListView) {
        this.invoiceView = view;
        compositeSubscription = CompositeSubscription()

        setDateSpinner()
    }

    override fun onDetachView() {
        if (this.invoiceView == null)
            return
        invoiceView = null
        compositeSubscription?.unsubscribe()
        compositeSubscription = null
    }

    private fun setDateSpinner() {

        dateList.add("Invoices for Today")
        dateList.add("Invoices for Yesterday")

        invoiceView?.setDateSpinner(dateList)
    }

    private fun getRetailerList(): ArrayList<String> {
        vehicleList.clear()
        val result =
            filteredList
                .groupBy { it.vehicleId }

        vehicleList.addAll(result.keys)
        return vehicleList
    }

    override fun omInvoiceDateRangeChanged(position: Int) {
        today = (position == 0)

        getInvoiceList()

    }

    override fun onVehicelSelected(position: Int) {
        vehicle = vehicleList[position]
        applyFilter()

    }

    override fun searchByInvoiceNumber(str: String) {
        invoice_number = str
        applyFilter()

    }

    override fun clearFilter() {
        vehicle = ""
        invoice_number = ""
        applyFilter()
    }


    private fun applyFilter() {

        filteredList.clear()
        filteredList.addAll(
            when {
                vehicle.isNotEmpty() && invoice_number.isNotEmpty() -> invoiceList.filter {
                    it.vehicleId == vehicle && it.invoiceId.toLowerCase()
                        .contains(invoice_number.toLowerCase())
                }
                invoice_number.isNotEmpty() -> invoiceList.filter {
                    it.invoiceId.toLowerCase().contains(invoice_number.toLowerCase())
                }
                vehicle.isNotEmpty() -> invoiceList.filter { it.vehicleId == vehicle }
                else -> invoiceList
            }
        )
        invoiceView?.refreshList(filteredList.size)
    }

    private fun getFromAndToDates() {
        var cal1 = Calendar.getInstance()
        cal1.set(Calendar.HOUR_OF_DAY, 0)
        cal1.set(Calendar.MINUTE, 0)
        cal1.set(Calendar.SECOND, 0)

        to = if (today) {
            cal1.add(Calendar.DATE, 1)
            cal1.time
        } else {
            cal1.time
        }

        cal1 = Calendar.getInstance()
        cal1.set(Calendar.HOUR_OF_DAY, 0)
        cal1.set(Calendar.MINUTE, 0)
        cal1.set(Calendar.SECOND, 0)

        from = if (today) {
            cal1.time
        } else {
            cal1.add(Calendar.DATE, -1)
            cal1.time
        }
    }

    override fun getInvoiceList() {
        getFromAndToDates()
        compositeSubscription?.add(
            sortationApiInteractor.getInvoiceList(
                getDateString(from!!),
                getDateString(to!!)
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ list ->
                    invoiceList.clear()
                    invoiceList.addAll(list)

                    applyFilter()
                    invoiceView?.setDestinationSpinner(getRetailerList())

                }, { error ->
                    invoiceView?.onFailure(error)

                })
        )
    }

    override fun getCount(): Int {
        return filteredList.size

    }

    override fun onBindInvoiceRowView(position: Int, invoiceRowView: InvoiceRowView) {
        val invoice =
            filteredList[position]


        invoiceRowView.setInvoiceId(invoice.invoiceId)
        invoiceRowView.setTime(getTimeFromISODate(invoice.createdOn))

        if (invoice.wayWillNumberUrl == null)
            invoiceRowView.hideEwayBill()

        invoiceRowView.setOnClickListeners()
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

    override fun onPrintInvoiceClicked(position: Int) {
        val invoice = filteredList[position]
        if (invoice.invoiceUrl != null)
            invoiceView?.onPrintUrlFetched("Consoliated Invoice PDF", invoice.invoiceUrl)
        else {
            invoiceView?.showProgressBar()
            getInvoice(invoice)
        }
    }

    private fun getInvoice(invoice: Invoice) {
        compositeSubscription?.add(sortationApiInteractor.getInvoiceUrlv2(invoice.invoiceId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (it.invoiceUrl != null)
                    invoiceView?.onPrintUrlFetched("Consoliated Invoice PDF", it.invoiceUrl)
            }, {
                invoiceView?.onFailure(it)

            })
        )
    }

    override fun onEwayBillClicked(position: Int) {
        val invoice = filteredList[position]
        invoiceView?.onPrintUrlFetched("Eway Bill PDF", invoice.wayWillNumberUrl!!)
    }

    private fun getDateString(date: Date): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        //Log.d("InvoiceList", dateFormat.format(date))
        return dateFormat.format(date)
    }


}