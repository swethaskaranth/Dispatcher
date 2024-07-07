package com.goflash.dispatch.features.audit.ui

import android.os.Bundle
import android.view.View
import com.goflash.dispatch.R
import com.goflash.dispatch.SortationApplication
import com.goflash.dispatch.databinding.ActivityAuditDetailsBinding
import com.goflash.dispatch.di.components.DaggerActivityComponents
import com.goflash.dispatch.features.audit.presenter.AuditSummaryDetailPresenter
import com.goflash.dispatch.features.audit.view.AuditSummaryDetailView
import com.goflash.dispatch.ui.activity.BaseActivity
import javax.inject.Inject

class AuditSummaryDetailActivity : BaseActivity(), View.OnClickListener,
    AuditSummaryDetailView {

    @Inject
    lateinit var mPresenter: AuditSummaryDetailPresenter

    lateinit var binding: ActivityAuditDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuditDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initDagger()
        initViews()
    }

    private fun initDagger() {
        DaggerActivityComponents.builder()
            .networkComponent((application as SortationApplication).getNetworkComponent())
            .build().inject(this@AuditSummaryDetailActivity)

        mPresenter.onAttachView(this, this)
        mPresenter.sendIntent(intent)
    }

    private fun initViews() {

        binding.toolBar1.toolbarTitle.text = getString(R.string.summary)

        binding.toolBar1.iVProfileHome.setOnClickListener(this)
    }

    override fun onClick(v: View?) {

        when (v?.id) {

            R.id.iVProfileHome -> finish()

        }
    }

    override fun setStartTime(time: String) {
        binding.orderDetail.tvstartTime.text = time
    }

    override fun setEndTime(time: String) {
        binding.orderDetail.tvEndTime.text = time
    }

    override fun setNameAndAsset(name: String?, asset: String?) {
        binding.orderDetail.assetName.text = "$asset - "
        binding.orderDetail.userName.text = name
    }

    override fun setBagCount(scanned: Long, expected: Long) {
        binding.tvBagCount.text =
            String.format(getString(R.string.bag_scanned_count), scanned.toInt(), expected.toInt())
    }

    override fun setShipmentCount(scanned: Long, expected: Long) {
        binding.tvShipmentCount.text =
            String.format(getString(R.string.bag_scanned_count), scanned.toInt(), expected.toInt())
    }

    override fun setBagShortAndExtraCount(short: Long, extra: Long) {
        if(short == 0L && extra == 0L) {
            binding.llScanBags.visibility = View.GONE
            return
        }
        if (short == 0L)
            binding.llshortBag.visibility = View.GONE
        else
            binding.tvshortBagCount.text = "$short"

        if (extra == 0L)
            binding.llextraBag.visibility = View.GONE
        else
            binding.tvextraBagCount.text = "$extra"
    }

    override fun setShipmentShortAndExtraCount(short: Long, extra: Long) {
        if(short == 0L && extra == 0L) {
            binding.llScanShipment.visibility = View.GONE
            return
        }
        if (short == 0L)
            binding.llmissingShipment.visibility = View.GONE
        else
            binding.tvmissingShipment.text = "$short"

        if (extra == 0L)
            binding.llextraShipment.visibility = View.GONE
        else
            binding.tvextraShipment.text = "$extra"
    }

    override fun onFailure(error: Throwable?) {
        hideProgress()
        processError(error)
    }

    override fun onDestroy() {
        super.onDestroy()
        mPresenter.onDetachView()
    }

}
