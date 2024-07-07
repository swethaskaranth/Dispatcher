package com.goflash.dispatch.ui.activity

import android.content.Intent
import android.os.Bundle
import com.goflash.dispatch.SortationApplication
import com.goflash.dispatch.databinding.LayoutMaintenanceModeBinding
import com.goflash.dispatch.di.components.DaggerActivityComponents
import com.goflash.dispatch.presenter.MaintenancePresenter
import com.goflash.dispatch.presenter.views.MaintenanceView
import javax.inject.Inject

class MaintenanceActivity : BaseActivity(), MaintenanceView {

    @Inject
    lateinit var mPresenter: MaintenancePresenter

    private lateinit var binding: LayoutMaintenanceModeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LayoutMaintenanceModeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initDagger()

        binding.swipeRefreshLayout.setOnRefreshListener {
            mPresenter.checkHealthStatus()
        }
    }

    private fun initDagger() {
        DaggerActivityComponents.builder()
            .networkComponent((application as SortationApplication).getNetworkComponent())
            .build().inject(this)
        mPresenter.onAttachView(this, this)
    }

    override fun onMaintenanceModeFetched(on: Boolean) {
        binding.swipeRefreshLayout.isRefreshing = false
        if (!on)
            finish()

    }

    override fun onBackPressed() {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    override fun onFailure(error: Throwable?) {
        binding.swipeRefreshLayout.isRefreshing = false
        processError(error)
    }

    override fun onDestroy() {
        super.onDestroy()
        mPresenter.onDetachView()
    }
}