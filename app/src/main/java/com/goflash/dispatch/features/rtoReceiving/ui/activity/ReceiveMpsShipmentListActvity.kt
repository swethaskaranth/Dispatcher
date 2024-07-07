package com.goflash.dispatch.features.rtoReceiving.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.goflash.dispatch.R
import com.goflash.dispatch.SortationApplication
import com.goflash.dispatch.app_constants.run_id
import com.goflash.dispatch.data.InwardRunItem
import com.goflash.dispatch.databinding.LayoutMpsShipmentsBinding
import com.goflash.dispatch.di.components.DaggerActivityComponents
import com.goflash.dispatch.features.rtoReceiving.listeners.RaiseIssueListener
import com.goflash.dispatch.features.rtoReceiving.listeners.ScanItemSelectionListener
import com.goflash.dispatch.features.rtoReceiving.presenter.ReceiveMpsShipmentListPresenter
import com.goflash.dispatch.features.rtoReceiving.ui.adapter.ReceiveMpsShipmentAdapter
import com.goflash.dispatch.features.rtoReceiving.ui.fragments.BottomSheetReasonsFragment
import com.goflash.dispatch.features.rtoReceiving.view.ReceiveMpsShipmentListView
import com.goflash.dispatch.ui.activity.BaseActivity
import com.goflash.dispatch.ui.itemDecoration.VerticalMarginItemDecoration
import javax.inject.Inject

class ReceiveMpsShipmentListActvity: BaseActivity(), ReceiveMpsShipmentListView, ScanItemSelectionListener,
    RaiseIssueListener {

    lateinit var binding : LayoutMpsShipmentsBinding

    @Inject
    lateinit var mPresenter: ReceiveMpsShipmentListPresenter

    private var adapter : ReceiveMpsShipmentAdapter? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LayoutMpsShipmentsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        initDagger()

    }

    private fun initDagger(){
        DaggerActivityComponents.builder().networkComponent((application as SortationApplication).getNetworkComponent())
            .build().inject(this)

        mPresenter.onAttach(this, this)
        mPresenter.sendIntent(intent)
    }

    private fun initViews(){
        binding.ivClose.setOnClickListener{
            finish()
        }
    }

    override fun setupData(count: Int, list: List<InwardRunItem>) {
        binding.tvNoOfBoxes.text = String.format(getString(R.string._no_boxes_in_this_shipment), count)
        binding.rvBoxes.layoutManager = LinearLayoutManager(this)
        binding.rvBoxes.addItemDecoration(VerticalMarginItemDecoration(resources.getDimension(
            R.dimen.margin_8
        ).toInt()))
        adapter = ReceiveMpsShipmentAdapter(this, list, this)
        binding.rvBoxes.adapter = adapter

    }

    override fun onExceptionsFetched(waybillNumber: String, status: String?, list: List<String>) {
        hideProgress()
        val bottomsheet = BottomSheetReasonsFragment()
        val bundle = Bundle()
        bundle.putString("wayBillNumber", waybillNumber)
        bundle.putString("status", status)
        bundle.putStringArrayList("exceptions", ArrayList(list))
        bottomsheet.arguments = bundle
        bottomsheet.show(supportFragmentManager, bottomsheet.tag)
    }

    override fun onActionButtonClicked(position: Int) {
        showProgress()
        mPresenter.getExceptionReasons(position)
    }

    override fun onStatusSelected(status: String, wayBillNumber: String, exceptions: List<String>) {
        showProgress()
        mPresenter.onReasonSelected(status, wayBillNumber, exceptions)
    }

    override fun onStatusUpdated(runItem: InwardRunItem) {
        hideProgress()
        adapter?.updateRunItem(runItem)
    }

    override fun onFailure(error: Throwable?) {
        processError(error)
    }
}