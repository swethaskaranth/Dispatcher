package com.goflash.dispatch.features.lastmile.settlement.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.goflash.dispatch.R
import com.goflash.dispatch.SortationApplication
import com.goflash.dispatch.app_constants.refresh
import com.goflash.dispatch.app_constants.sprinter_name
import com.goflash.dispatch.app_constants.trip_id
import com.goflash.dispatch.data.FmPickedShipment
import com.goflash.dispatch.data.UndeliveredShipmentDTO
import com.goflash.dispatch.databinding.LayoutReceivedItemSummaryBinding
import com.goflash.dispatch.di.components.DaggerActivityComponents
import com.goflash.dispatch.features.lastmile.settlement.listeners.SelectedListener
import com.goflash.dispatch.features.lastmile.settlement.presenter.FmPickupReviewPresenter
import com.goflash.dispatch.features.lastmile.settlement.ui.adapter.FmReviewAdapter
import com.goflash.dispatch.features.lastmile.settlement.view.FmPickupReviewView
import com.goflash.dispatch.ui.activity.BaseActivity
import com.goflash.dispatch.ui.itemDecoration.MarginItemDecoration
import javax.inject.Inject

class FmPickupReviewActivity : BaseActivity(), FmPickupReviewView, SelectedListener,
    View.OnClickListener {

    @Inject
    lateinit var mPresenter: FmPickupReviewPresenter

    private var tripId: Long? = null

    private var sprinter: String? = null

    private lateinit var binding: LayoutReceivedItemSummaryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LayoutReceivedItemSummaryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tripId = intent.getLongExtra(trip_id, 0)
        sprinter = intent.getStringExtra(sprinter_name)

        initDagger()
        initViews()
    }

    private fun initDagger() {
        DaggerActivityComponents.builder()
            .networkComponent((application as SortationApplication).getNetworkComponent())
            .build().inject(this@FmPickupReviewActivity)

        mPresenter.onAttachView(this, this)

        mPresenter.getShipments(intent.getStringExtra("originName")?:"")

    }

    private fun initViews() {
        binding.toolBar.toolbarTitle.text = getString(R.string.review_balance_shipments)
        binding.toolBar.tvSprinter.text = intent.getStringExtra("originName")
        binding.labelSummary.visibility = View.GONE

        binding.btnPaymentLayout.btnPayment.text = getString(R.string.proceed)
        binding.btnPaymentLayout.btnPayment.setOnClickListener(this)
        binding.toolBar.ivBack.setOnClickListener(this)

        binding.rvItems.layoutManager = LinearLayoutManager(this)
        binding.rvItems.addItemDecoration(
            MarginItemDecoration(
                resources.getDimension(R.dimen.margin_10).toInt()
            )
        )

    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.btn_payment, R.id.ivBack -> {
                gotoSummaryActivity()
            }
        }
    }

    override fun onShipmentsFetched(list: List<FmPickedShipment>) {
        binding.rvItems.adapter = FmReviewAdapter(this, list, this)
    }

    override fun onFmShipmentReasonSelected(position: Int, reason: String) {
        mPresenter.onReasonSelected(position, reason)
    }

    private fun gotoSummaryActivity() {
        val intent = Intent(this, FmSummaryActivity::class.java)
        intent.putExtra(trip_id, tripId)
        intent.putExtra(sprinter_name, sprinter)
        intent.putExtra(refresh, true)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        mPresenter.onDetachView()
    }


}