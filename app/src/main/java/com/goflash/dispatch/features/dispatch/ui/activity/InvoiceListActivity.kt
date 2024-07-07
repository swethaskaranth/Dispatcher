package com.goflash.dispatch.features.dispatch.ui.activity

import android.content.Intent
import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintJob
import android.print.PrintManager
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.goflash.dispatch.R
import com.goflash.dispatch.SortationApplication
import com.goflash.dispatch.databinding.ActivityInvoiceListBinding
import com.goflash.dispatch.di.components.DaggerActivityComponents
import com.goflash.dispatch.features.dispatch.presenter.InvoiceListPresenter
import com.goflash.dispatch.features.dispatch.ui.adapter.InvoiceAdapter
import com.goflash.dispatch.features.dispatch.view.InvoiceListView
import com.goflash.dispatch.service.PrintJobMonitorService
import com.goflash.dispatch.features.dispatch.ui.interfaces.InvoiceListController
import com.goflash.dispatch.ui.activity.BaseActivity
import com.goflash.dispatch.ui.interfaces.OnPrintFinishListener
import com.goflash.dispatch.ui.itemDecoration.MarginItemDecoration
import com.pharmeasy.bolt.ui.adapters.CustomPrintDocumentAdapter
import javax.inject.Inject


class InvoiceListActivity : BaseActivity(), InvoiceListView, View.OnClickListener, OnPrintFinishListener {

    @Inject
    lateinit var presenter: InvoiceListPresenter

    private var mgr: PrintManager? = null

    private var dateSpinnerInitialized = false
    private var vehicleSpinnerInitialized = false

    private lateinit var binding: ActivityInvoiceListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInvoiceListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mgr = getSystemService(PRINT_SERVICE) as PrintManager

        initDagger()
        initViews()
    }

    private fun initViews() {

        binding.rvInvoice.layoutManager = LinearLayoutManager(this)
        binding.rvInvoice.adapter = InvoiceAdapter(this, presenter as InvoiceListController)
        binding.rvInvoice.addItemDecoration(MarginItemDecoration(resources.getDimension(R.dimen.margin_10).toInt()))

        binding.ivBack.setOnClickListener(this)

        binding.rvInvoice.visibility = View.GONE
        binding.invoiceLabel.text = String.format(getString(R.string.invoices_label), 0)

        val textWatcher = object : TextWatcher{
            override fun afterTextChanged(str: Editable?) {
                if(str?.length!! > 0)
                    presenter.searchByInvoiceNumber(str.toString())
                else
                    presenter.clearFilter()
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

        }

        binding.searchInvNumEt.addTextChangedListener(textWatcher)

        showProgress()
        presenter.getInvoiceList()

    }

    override fun setDateSpinner(list : MutableList<String>){

        binding.invoiceSpinner.adapter = ArrayAdapter<String>(this, R.layout.spinner_item, list)
        binding.invoiceSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {
                if(!dateSpinnerInitialized)
                    dateSpinnerInitialized = true
                else{
                    vehicleSpinnerInitialized = false
                        presenter.omInvoiceDateRangeChanged(pos)
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }
        }
    }


    private fun initDagger() {
        DaggerActivityComponents.builder().networkComponent((application as SortationApplication).getNetworkComponent())
                .build().inject(this@InvoiceListActivity)

        presenter.onAttachView(this@InvoiceListActivity, this)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.ivBack -> finish()
        }

    }

    override fun setDestinationSpinner(list: ArrayList<String>) {
        list.add(0,"Search by Vehicle ID")

        binding.retailerSpinner.adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list)
        binding.retailerSpinner.setSelection(0)

        binding.retailerSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                if(!vehicleSpinnerInitialized)
                    vehicleSpinnerInitialized = true
                else {
                    if (pos != 0) {
                        presenter.onVehicelSelected(pos)

                    }else{
                        presenter.clearFilter()
                    }
                }

            }

        }
    }


    override fun refreshList(count : Int) {
        hideProgress()
        binding.invoiceLabel.text = String.format(getString(R.string.invoices_label), count)
        if(count > 0 ) {
            binding.rvInvoice.visibility = View.VISIBLE
            binding.rvInvoice.adapter?.notifyDataSetChanged()
        }else{
            binding.rvInvoice.visibility = View.GONE
        }

    }


    override fun onFailure(error: Throwable?) {
        hideProgress()
        processError(error)
    }


    override fun onPrintUrlFetched(message : String, result: String) {
        hideProgress()
        print(
                message,
                CustomPrintDocumentAdapter(applicationContext, result, this),
                PrintAttributes.Builder().build()
        )
    }

    private fun print(
            name: String, adapter: PrintDocumentAdapter,
            attrs: PrintAttributes
    ): PrintJob {

        startService(Intent(this, PrintJobMonitorService::class.java))

        return mgr!!.print(name, adapter, attrs)
    }

    override fun onPrintFinished() {
        //Toast.makeText(this, resources.getString(R.string.success), Toast.LENGTH_LONG).show()

    }

    override fun showProgressBar() {
        showProgress()
    }

    override fun hideProgressBar() {
        hideProgress()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDetachView()
    }
}