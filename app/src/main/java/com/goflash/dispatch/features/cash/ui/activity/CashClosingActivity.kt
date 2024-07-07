package com.goflash.dispatch.features.cash.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager

import com.goflash.dispatch.R
import com.goflash.dispatch.SortationApplication
import com.goflash.dispatch.app_constants.CASH_CLOSING_ID
import com.goflash.dispatch.app_constants.CASH_CLOSING_TOTAL
import com.goflash.dispatch.data.CashClosingDetails
import com.goflash.dispatch.data.CashDetails
import com.goflash.dispatch.databinding.ActivityCashClosingBinding
import com.goflash.dispatch.di.components.DaggerActivityComponents
import com.goflash.dispatch.features.cash.presenter.CashPresenter
import com.goflash.dispatch.features.cash.ui.adapter.CashClosingAdapter
import com.goflash.dispatch.features.cash.ui.listener.OnShipmentSelectedListener
import com.goflash.dispatch.features.cash.view.CashView
import com.goflash.dispatch.ui.activity.BaseActivity
import javax.inject.Inject

class CashClosingActivity : BaseActivity(), CashView, OnShipmentSelectedListener, View.OnClickListener {

    @Inject
    lateinit var mPresenter: CashPresenter

    private var list: MutableList<CashDetails> = mutableListOf();

    var page = 0
    var size = 10
    var limit = 0

    private lateinit var binding: ActivityCashClosingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        initDagger()
        super.onCreate(savedInstanceState)
        binding = ActivityCashClosingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(findViewById(R.id.toolbar))

        initView()
    }

    private fun initView() {

        binding.createSummaryIcon.setOnClickListener(this)
        binding.ivClear.setOnClickListener(this)
        binding.contentCashClosing.rvCashClosing.layoutManager = LinearLayoutManager(this).apply { isSmoothScrollbarEnabled = true}

        showProgress()
        mPresenter.getAllCash(page, size)

        binding.contentCashClosing.scrollView.setOnScrollChangeListener { v: NestedScrollView?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->
            //check condition
            if(scrollY == v?.getChildAt(0)!!.measuredHeight - v.measuredHeight){
                //when reached to end items

                if(page < limit) {
                    page++
                    binding.contentCashClosing.progressBar.visibility = View.VISIBLE
                    mPresenter.getAllCash(page, size)
                }
            }
        }

    }

    private fun initDagger() {
        DaggerActivityComponents.builder()
            .networkComponent((application as SortationApplication).getNetworkComponent())
            .build().inject(this@CashClosingActivity)

        mPresenter.onAttach(this, this@CashClosingActivity)
    }

    override fun onSuccess(cashClosingDetails: CashClosingDetails) {
        hideProgress()
        binding.contentCashClosing.progressBar.visibility = View.GONE

        limit = cashClosingDetails.pages

        list.addAll(cashClosingDetails.data)
        binding.contentCashClosing.rvCashClosing.adapter = CashClosingAdapter(this, list, this)

    }

    override fun onFailure(error: Throwable?) {
        hideProgress()
    }

    override fun onDestroy() {
        mPresenter.onDeAttach()
        super.onDestroy()

    }

    override fun onShipmentSelected(shipments: List<CashDetails>) {

    }

    override fun onShipmentUnselected(shipments: List<CashDetails>) {

    }

    override fun showCashBreakup(cashClosingId: String,totalCash: String) {
        val intent = Intent(this,
            CashCollectionBreakupActivity::class.java)
        intent.putExtra(CASH_CLOSING_ID,cashClosingId)
        intent.putExtra(CASH_CLOSING_TOTAL,totalCash)
        startActivity(intent)
    }

    private fun openActivity(){
        val intent = Intent(this, CreateSummaryActivity::class.java)
        startActivity(intent)
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.create_summary_icon -> openActivity()
            R.id.ivClear -> finish()
        }
    }
}