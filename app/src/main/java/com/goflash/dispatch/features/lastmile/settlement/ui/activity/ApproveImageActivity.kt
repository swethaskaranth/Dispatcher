package com.goflash.dispatch.features.lastmile.settlement.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.viewpager2.widget.ViewPager2
import com.goflash.dispatch.R
import com.goflash.dispatch.SortationApplication
import com.goflash.dispatch.data.AckSlipDto
import com.goflash.dispatch.databinding.LayoutApproveImageBinding
import com.goflash.dispatch.di.components.DaggerActivityComponents
import com.goflash.dispatch.features.lastmile.settlement.presenter.ApproveImagePresenter
import com.goflash.dispatch.features.lastmile.settlement.ui.adapter.ImageAdapter
import com.goflash.dispatch.features.lastmile.settlement.view.ApproveImageView
import com.goflash.dispatch.ui.activity.BaseActivity
import com.google.android.material.snackbar.Snackbar
import javax.inject.Inject

class ApproveImageActivity : BaseActivity(), ApproveImageView, View.OnClickListener {

    @Inject
    lateinit var mPresenter: ApproveImagePresenter

    private var tripId: Long? = null
    private var sprinterName: String? = null
    lateinit var lbn: String

    private var currentIndex = 0

    private var totalCount = 0

    private lateinit var binding: LayoutApproveImageBinding

    private var callback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            mPresenter.onPageSelected(position)
            currentIndex = position
            enableOrDisablePrev()
            enableOrDisableNext()
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LayoutApproveImageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tripId = intent.getLongExtra("tripId", -1)
        sprinterName = intent.getStringExtra("sprinterName")
        lbn = intent.getStringExtra("lbn") ?: ""

        initDagger()
        initViews()
    }

    private fun initViews() {
        binding.toolBar.toolbarTitle.text = lbn
        binding.toolBar.tvSprinter.text =
            String.format(getString(R.string.trip_id_with_sprinter), tripId, sprinterName)
        binding.toolBar.ivBack.setOnClickListener(this)
        binding.prev.setOnClickListener(this)
        binding.next.setOnClickListener(this)

        binding.btnPaymentLayout.btnApprove.text = getString(R.string.approve_image)
        binding.btnPaymentLayout.btnReject.text = getString(R.string.reject)

        binding.btnPaymentLayout.btnApprove.setOnClickListener(this)
        binding.btnPaymentLayout.btnReject.setOnClickListener(this)

    }

    private fun initDagger() {
        DaggerActivityComponents.builder()
            .networkComponent((application as SortationApplication).getNetworkComponent()).build()
            .inject(this@ApproveImageActivity)

        mPresenter.onAttach(this, this)
        mPresenter.getAckSlipsForLBN(tripId!!, lbn)
    }

    override fun onAckSlipsFetched(slips: MutableList<AckSlipDto>) {
        if (slips.isEmpty()) {
            closeActivity()
            return
        }

        val adapter = ImageAdapter(this@ApproveImageActivity, slips)
        binding.imageViewPager.adapter = adapter

        binding.imageViewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        binding.imageViewPager.registerOnPageChangeCallback(callback)

        totalCount = slips.size

        currentIndex = intent?.getIntExtra("position", 0) ?: 0
        binding.imageViewPager.currentItem = currentIndex

        if (totalCount > 1) {
            binding.prev.visibility = View.VISIBLE
            binding.next.visibility = View.VISIBLE

            enableOrDisablePrev()
            enableOrDisableNext()
        }
    }

    private fun enableOrDisablePrev() {
        if (currentIndex == 0) {
            binding.prev.isEnabled = false
            binding.prev.setImageResource(R.drawable.ic_right_arrow)
        } else {
            binding.prev.isEnabled = true
            binding.prev.setImageResource(R.drawable.ic_prev)
        }
        binding.prev.rotation = 180f
    }

    private fun enableOrDisableNext() {
        if (currentIndex == totalCount - 1) {
            binding.next.isEnabled = false
            binding.next.setImageResource(R.drawable.ic_right_arrow)
        } else {
            binding.next.isEnabled = true
            binding.next.setImageResource(R.drawable.ic_prev)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivBack -> finish()
            R.id.prev -> {
                if (currentIndex != 0) {
                    binding.imageViewPager.currentItem = --currentIndex
                    enableOrDisablePrev()
                    enableOrDisableNext()
                }
            }
            R.id.next -> {
                if (currentIndex != totalCount) {
                    binding.imageViewPager.currentItem = ++currentIndex
                    enableOrDisablePrev()
                    enableOrDisableNext()
                }
            }
            R.id.btnApprove -> mPresenter.onApproveImageClicked()
            R.id.btnReject -> mPresenter.onRejectImageClicked()
        }
    }

    override fun onImageApproved(message: String) {
        Snackbar.make(binding.btnPaymentLayout.btnPaymentLayout, message, Snackbar.LENGTH_SHORT).show()
        // mPresenter.getAckSlipsForLBN(tripId!!, lbn)
    }

    override fun closeActivity() {
        val intent = Intent(this@ApproveImageActivity, Step4VerifyImagesActivity::class.java)
        intent.putExtra("tripId", tripId.toString())
        intent.putExtra("sprinterName", sprinterName)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.imageViewPager.unregisterOnPageChangeCallback(callback)
    }
}