package com.goflash.dispatch.features.rtoReceiving.ui.activity

import android.content.Intent
import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintJob
import android.print.PrintManager
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.goflash.dispatch.R
import com.goflash.dispatch.SortationApplication
import com.goflash.dispatch.data.InwardRunItem
import com.goflash.dispatch.databinding.LayoutRunsheetBinding
import com.goflash.dispatch.di.components.DaggerActivityComponents
import com.goflash.dispatch.features.rtoReceiving.presenter.RunsheetPresenter
import com.goflash.dispatch.features.rtoReceiving.ui.adapter.RunsheetAdapter
import com.goflash.dispatch.features.rtoReceiving.view.RunsheetView
import com.goflash.dispatch.listeners.OnItemSelected
import com.goflash.dispatch.service.PrintJobMonitorService
import com.goflash.dispatch.ui.activity.BaseActivity
import com.goflash.dispatch.ui.activity.HomeActivity
import com.goflash.dispatch.ui.interfaces.OnPrintFinishListener
import com.goflash.dispatch.ui.itemDecoration.VerticalMarginItemDecoration
import com.goflash.dispatch.util.getDate
import com.pharmeasy.bolt.ui.adapters.CustomPrintDocumentAdapter
import javax.inject.Inject

class RunsheetActivity : BaseActivity(), RunsheetView, View.OnClickListener, OnPrintFinishListener, OnItemSelected {

    lateinit var binding: LayoutRunsheetBinding

    @Inject
    lateinit var mPresenter: RunsheetPresenter

    private var mgr: PrintManager? = null

    private var home = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = LayoutRunsheetBinding.inflate(layoutInflater)
        setContentView(binding.root)

        home = intent.getBooleanExtra("home",false)

        mgr = getSystemService(PRINT_SERVICE) as PrintManager

        initViews()
        initDagger()
    }

    private fun initViews() {
        binding.toolbar.iVProfileHome.setOnClickListener(this)
        binding.btnPrint.setOnClickListener(this)

        binding.rvRunItems.layoutManager = LinearLayoutManager(this)
    }

    private fun initDagger() {
        DaggerActivityComponents.builder()
            .networkComponent((application as SortationApplication).getNetworkComponent()).build()
            .inject(this@RunsheetActivity)
        mPresenter.onAttach(this, this)
        showProgress()
        mPresenter.sendIntent(intent)
    }

    override fun onInwardRunFetched(createdOn: String?,partnerName: String?, count: Int, items: List<InwardRunItem>) {
        hideProgress()

        binding.toolbar.toolbarTitle.text =
            String.format(getString(R.string.runsheet_for), createdOn?.let { getDate(it) })
        binding.tvCount.text = "$count"

        binding.rvRunItems.adapter = RunsheetAdapter(this, items, this)
        binding.rvRunItems.addItemDecoration(
            VerticalMarginItemDecoration(
                resources.getDimension(
                    R.dimen.margin_13
                ).toInt()
            )
        )
        binding.partnerName.tvPartnerName.text = partnerName
        binding.partnerName.tvShipmentCount.text = "$count"

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.iVProfileHome -> onBackPressed()
            R.id.btnPrint -> mPresenter.getRunsheetUrl()
        }
    }

    override fun onItemSelected(position: Int) {
        mPresenter.getMpsRunItems(position)
    }

    override fun onMpsRunItemsFetched(total: Int, items: List<InwardRunItem>) {
        val intent = Intent(this, MpsInwardRunItemsActivity::class.java)
        intent.putExtra("total",total)
        intent.putExtra("mpsRunItems", items.toTypedArray())
        startActivity(intent)
    }

    override fun onUrlFetched(url: String) {
        hideProgress()

        if(home && url.isEmpty()) {
            onFailure(Throwable("Runsheet generation is in progress. Please try after sometime."))
            return
        }
        print(
            "Runsheet PDF",
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

    override fun onFailure(error: Throwable?) {
        hideProgress()
        processError(error)
    }

    override fun onBackPressed() {
        if(home) {
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }else
            super.onBackPressed()
    }
}