package com.goflash.dispatch.features.lastmile.settlement.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.goflash.dispatch.R
import com.goflash.dispatch.SortationApplication
import com.goflash.dispatch.app_constants.*
import com.goflash.dispatch.data.FmPickedShipment
import com.goflash.dispatch.databinding.LayoutReceivedItemSummaryBinding
import com.goflash.dispatch.di.components.DaggerActivityComponents
import com.goflash.dispatch.features.lastmile.settlement.presenter.FmSummaryPresenter
import com.goflash.dispatch.features.lastmile.settlement.ui.adapter.FmSummaryAdapter
import com.goflash.dispatch.features.lastmile.settlement.view.FmSummaryView
import com.goflash.dispatch.features.lastmile.tripCreation.listeners.OnItemSelctedListener
import com.goflash.dispatch.ui.activity.BaseActivity
import com.goflash.dispatch.ui.itemDecoration.MarginItemDecoration
import javax.inject.Inject

class FmSummaryActivity : BaseActivity(), OnItemSelctedListener,
    View.OnClickListener, FmSummaryView {

    @Inject
    lateinit var mPresenter: FmSummaryPresenter

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

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent?.getBooleanExtra(refresh, false) == true)
            mPresenter.getShipments(tripId!!)
    }

    private fun initDagger() {
        DaggerActivityComponents.builder()
            .networkComponent((application as SortationApplication).getNetworkComponent())
            .build().inject(this@FmSummaryActivity)

        mPresenter.onAttachView(this, this)

        mPresenter.getShipments(tripId!!)

    }

    private fun initViews() {
        binding.toolBar.toolbarTitle.text = getString(R.string.received_fm_picked)
        binding.toolBar.tvSprinter.text = String.format(getString(R.string.trip_id_with_sprinter), tripId, sprinter)
        binding.labelSummary.text = getString(R.string.fm_pickup_summary)

        binding.btnPaymentLayout.btnPayment.text = getString(R.string.proceed)

        binding.toolBar.ivBack.setOnClickListener(this)
        binding.btnPaymentLayout.btnPayment.setOnClickListener(this)

        binding.rvItems.layoutManager = LinearLayoutManager(this)
        binding.rvItems.addItemDecoration(
            MarginItemDecoration(
                resources.getDimension(R.dimen.margin_10).toInt()
            )
        )
    }

    override fun onShipmentsFetched(map: Map<String, List<FmPickedShipment>>) {
        binding.rvItems.adapter = FmSummaryAdapter(this, map, this)
    }


    override fun onShipmentSelected(originName: String) {
        val intent = Intent(this, FmPickupReviewActivity::class.java)
        intent.putExtra(trip_id, tripId)
        intent.putExtra(sprinter_name, sprinter)
        intent.putExtra("originName", originName)
        startActivity(intent)
    }

    override fun enableOrDisableProceed(enable: Boolean) {
        binding.btnPaymentLayout.btnPayment.isEnabled = if (enable) {
            binding.btnPaymentLayout.btnPayment.setBackgroundResource(R.drawable.blue_button_background)
            true
        } else {
            binding.btnPaymentLayout.btnPayment.setBackgroundResource(R.drawable.grey_button_background)
            false
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_payment -> mPresenter.onNext(tripId!!)
            R.id.ivBack -> finish()
        }
    }

    override fun startStep3CashActivity() {
        val intent = Intent(this, Step3CashCollectionActivity::class.java)
        intent.putExtra("tripId", tripId.toString())
        intent.putExtra(sprinter_name, sprinter)
        startActivity(intent)
    }

    override fun startVerifyImageActivity() {
        val intent = Intent(this, Step4VerifyImagesActivity::class.java)
        intent.putExtra("tripId",tripId.toString())
        intent.putExtra(sprinter_name,sprinter)
        startActivity(intent)
    }

    override fun startAckDeliverySlipReconActivity() {
        val intent = Intent(this, AckDeliverySlipReconActivity::class.java)
        intent.putExtra("tripId", tripId.toString())
        intent.putExtra(sprinter_name, sprinter)
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        mPresenter.onDetachView()
    }
}