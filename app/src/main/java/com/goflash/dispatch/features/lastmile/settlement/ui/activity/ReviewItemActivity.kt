package com.goflash.dispatch.features.lastmile.settlement.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.goflash.dispatch.R
import com.goflash.dispatch.SortationApplication
import com.goflash.dispatch.app_constants.*
import com.goflash.dispatch.data.Item
import com.goflash.dispatch.databinding.LayoutReviewBalanceMedicinesBinding
import com.goflash.dispatch.di.components.DaggerActivityComponents
import com.goflash.dispatch.features.lastmile.settlement.listeners.ReviewItemListener
import com.goflash.dispatch.features.lastmile.settlement.presenter.ReviewItemPresenter
import com.goflash.dispatch.features.lastmile.settlement.ui.adapter.ReviewItemAdapter
import com.goflash.dispatch.features.lastmile.settlement.view.ReviewItemView
import com.goflash.dispatch.type.ReconStatus
import com.goflash.dispatch.ui.activity.BaseActivity
import com.goflash.dispatch.ui.itemDecoration.MarginItemDecoration
import javax.inject.Inject

class ReviewItemActivity : BaseActivity(), ReviewItemView, ReviewItemListener, View.OnClickListener {

    @Inject
    lateinit var mPresenter: ReviewItemPresenter

    private var tripId: Long? = null

    private var refId: String? = null

    private var partialDelivery = false

    private lateinit var binding: LayoutReviewBalanceMedicinesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LayoutReviewBalanceMedicinesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tripId = intent.getLongExtra(trip_id, 0)
        refId = intent.getStringExtra(reference_id)
        partialDelivery = intent?.getBooleanExtra(partial_delivery,false)?:false

        initDagger()
        initViews()
    }


    private fun initDagger() {
        DaggerActivityComponents.builder()
            .networkComponent((application as SortationApplication).getNetworkComponent())
            .build().inject(this@ReviewItemActivity)

        mPresenter.onAttachView(this, this)

        mPresenter.sendIntent(intent)
    }

    private fun initViews() {
        binding.toolBar.toolbarTitle.text = getString(R.string.review_balance_medicines)
        binding.toolBar.tvSprinter.text = String.format(getString(R.string.shipment_id), refId)

        binding.btnPaymentLayout.btnPayment.text = getString(R.string.done)

        binding.btnPaymentLayout.btnPayment.setOnClickListener (this)
        binding.toolBar.ivBack.setOnClickListener(this)


       binding.rvItems.layoutManager = LinearLayoutManager(this)
        binding.rvItems.addItemDecoration(
            MarginItemDecoration(
                resources.getDimension(R.dimen.margin_10).toInt()
            )
        )
    }

    override fun setItemDetails(name: String?, batch: String?, reason: String?) {
        binding.tvName.text = name
        binding.tvBatch.text = String.format(getString(R.string.batch_number), batch)
        binding.tvReason.text = String.format(getString(R.string.return_reason), reason)
    }

    override fun onItemsFetched(items: List<Item>) {
        binding.rvItems.adapter = ReviewItemAdapter(this, items,this,partialDelivery)
    }

    override fun onAcceptOrRejectSelected(
        position: Int,
        reconStatus: ReconStatus,
        reconReason: String,
        rejectRemarks: String
    ) {
        mPresenter.setAcceptReject(position,reconStatus,reconReason, rejectRemarks)
    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.btn_payment , R.id.ivBack -> {
                gotoSummaryActivity()
            }
        }
    }

    override fun showError(message: String) {
        showToast(this, message)
    }

    override fun onBackPressed() {
        gotoSummaryActivity()
    }

    private fun gotoSummaryActivity(){
        val intent = Intent(this,ItemSummaryActivity::class.java)
        intent.putExtra(refresh,true)
        intent.putExtra(trip_id, tripId)
        intent.putExtra(sprinter_name, sprinter)
        intent.putExtra(partial_delivery, partialDelivery)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        mPresenter.onDetachView()
    }


}