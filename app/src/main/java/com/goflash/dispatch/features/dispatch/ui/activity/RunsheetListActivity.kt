package com.goflash.dispatch.features.dispatch.ui.activity

import android.content.Intent
import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintJob
import android.print.PrintManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.goflash.dispatch.R
import com.goflash.dispatch.SortationApplication
import com.goflash.dispatch.data.MidMileDispatchedRunsheet
import com.goflash.dispatch.databinding.LayoutRunsheetListActivityBinding
import com.goflash.dispatch.di.components.DaggerActivityComponents
import com.goflash.dispatch.features.dispatch.presenter.RunsheetListPresenter
import com.goflash.dispatch.features.dispatch.ui.adapter.ManifestAdapter
import com.goflash.dispatch.features.dispatch.view.RunsheetListView
import com.goflash.dispatch.features.lastmile.tripCreation.listeners.OnItemSelctedListener
import com.goflash.dispatch.service.PrintJobMonitorService
import com.goflash.dispatch.ui.activity.BaseActivity
import com.goflash.dispatch.ui.interfaces.OnPrintFinishListener
import com.pharmeasy.bolt.ui.adapters.CustomPrintDocumentAdapter
import javax.inject.Inject

class RunsheetListActivity: BaseActivity(), RunsheetListView, OnItemSelctedListener,
    OnPrintFinishListener {

    @Inject
    lateinit var presenter: RunsheetListPresenter

    private var mgr: PrintManager? = null

    private lateinit var binding: LayoutRunsheetListActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LayoutRunsheetListActivityBinding.inflate(layoutInflater)

        setContentView(binding.root)

        mgr = getSystemService(PRINT_SERVICE) as PrintManager

        initDagger()
        initViews()
    }

    private fun initViews() {

        binding.toolBar1.toolbarTitle.text = getString(R.string.runsheet_list)
        binding.toolBar1.iVProfileHome.setOnClickListener { finish() }

        binding.rvRunsheet.layoutManager = LinearLayoutManager(this)

    }

    private fun initDagger() {
        DaggerActivityComponents.builder().networkComponent((application as SortationApplication).getNetworkComponent())
            .build().inject(this@RunsheetListActivity)

        presenter.onAttachView(this@RunsheetListActivity, this)
    }

    override fun onFailure(error: Throwable?) {
        processError(error)
    }

    override fun onRunsheetsFetched(list: List<MidMileDispatchedRunsheet>) {
        binding.rvRunsheet.adapter = ManifestAdapter(this, list, this)
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDetachView()
    }

    override fun onItemSelected(url: String, name:String) {
        print(
            name,
            CustomPrintDocumentAdapter(applicationContext, url, this),
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

    }


}