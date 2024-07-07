package com.goflash.dispatch.features.rtoReceiving.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.goflash.dispatch.R
import com.goflash.dispatch.SortationApplication
import com.goflash.dispatch.app_constants.run_id
import com.goflash.dispatch.data.InwardRun
import com.goflash.dispatch.databinding.LayoutReceiveShipmentsBinding
import com.goflash.dispatch.di.components.DaggerActivityComponents
import com.goflash.dispatch.features.rtoReceiving.listeners.RunitemSelectionListener
import com.goflash.dispatch.features.rtoReceiving.presenter.ReceiveShipmentListPresenter
import com.goflash.dispatch.features.rtoReceiving.ui.adapter.ReceiveShipmentsAdapter
import com.goflash.dispatch.features.rtoReceiving.view.ReceiveShipmentsListView
import com.goflash.dispatch.listeners.OnItemSelected
import com.goflash.dispatch.ui.activity.BaseActivity
import com.goflash.dispatch.ui.itemDecoration.VerticalMarginItemDecoration
import com.goflash.dispatch.util.PreferenceHelper
import javax.inject.Inject

class ReceiveShipmentsListActivity : BaseActivity(), ReceiveShipmentsListView, View.OnClickListener,
    RunitemSelectionListener {

    @Inject
    lateinit var mPresenter: ReceiveShipmentListPresenter

    lateinit var binding: LayoutReceiveShipmentsBinding

    private var disableComplete = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = LayoutReceiveShipmentsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        PreferenceHelper.init(this)

        disableComplete = intent.getBooleanExtra("disableComplete", false)

        initDagger()
        initViews()
    }

    private fun initDagger() {
        DaggerActivityComponents.builder()
            .networkComponent((application as SortationApplication).getNetworkComponent())
            .build().inject(this@ReceiveShipmentsListActivity)

        mPresenter.onAttach(this, this@ReceiveShipmentsListActivity)
        showProgress()
    }


    private fun initViews() {

        binding.toolbar.toolbarTitle.text = getString(R.string.receive_shipments)
        binding.toolbar.iVProfileHome.setOnClickListener(this)
        binding.btnStart.setOnClickListener(this)

        binding.tvReceivingHistory.text = String.format(getString(R.string.receiving_history_past_n_days), PreferenceHelper.dataForNumDays)


    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.iVProfileHome -> finish()
            R.id.btnStart -> if (disableComplete) processError(Throwable("Receiving Session in progress")) else takeToScanScreen()
        }
    }

    override fun onInwardRunsFetched(list: List<InwardRun>) {
        hideProgress()
        val adapter = ReceiveShipmentsAdapter(this, list, this)
        binding.rvReceiveShipments.layoutManager = LinearLayoutManager(this)
        binding.rvReceiveShipments.adapter = adapter
        binding.rvReceiveShipments.addItemDecoration(
            VerticalMarginItemDecoration(
                resources.getDimension(
                    R.dimen.margin_13
                ).toInt()
            )
        )
    }

    private fun takeToScanScreen() {
        val intent = Intent(this, ScanReceiveShipmentActivity::class.java)
        startActivity(intent)
    }

    override fun onRunItemSelcted(runId: Int) {
        val intent = Intent(this, RunsheetActivity::class.java)
        intent.putExtra(run_id, runId)
        startActivity(intent)
    }

    override fun onFailure(error: Throwable?) {
        hideProgress()
        processError(error)
    }

    override fun onDestroy() {
        super.onDestroy()
        mPresenter.onDetach()
    }


}