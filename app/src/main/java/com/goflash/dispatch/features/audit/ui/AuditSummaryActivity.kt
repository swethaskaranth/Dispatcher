package com.goflash.dispatch.features.audit.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.goflash.dispatch.R
import com.goflash.dispatch.SortationApplication
import com.goflash.dispatch.app_constants.audit_id
import com.goflash.dispatch.app_constants.end_time
import com.goflash.dispatch.app_constants.start_time
import com.goflash.dispatch.databinding.ActivityAuditSummaryBinding
import com.goflash.dispatch.di.components.DaggerActivityComponents
import com.goflash.dispatch.features.audit.presenter.AuditSummaryPresenter
import com.goflash.dispatch.features.audit.view.AuditSummaryView
import com.goflash.dispatch.ui.activity.BaseActivity
import com.goflash.dispatch.ui.adapter.AuditSummaryAdapter
import com.goflash.dispatch.util.PreferenceHelper
import javax.inject.Inject

class AuditSummaryActivity : BaseActivity(), View.OnClickListener,
    AuditSummaryView {

    @Inject
    lateinit var mPresenter: AuditSummaryPresenter

    lateinit var binding: ActivityAuditSummaryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuditSummaryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initDagger()
        initViews()
    }

    private fun initDagger() {
        DaggerActivityComponents.builder()
            .networkComponent((application as SortationApplication).getNetworkComponent())
            .build().inject(this@AuditSummaryActivity)

        mPresenter.onAttachView(this, this@AuditSummaryActivity)
    }

    private fun initViews() {

        binding.toolBar1.toolbarTitle.text = String.format(
            getString(R.string.audit_history),
            PreferenceHelper.assignedAssetName
        )
        binding.toolBar1.ivHistory.visibility = View.GONE

        binding.toolBar1.iVProfileHome.setOnClickListener(this)

        binding.rvSummary.layoutManager = LinearLayoutManager(this)
        binding.rvSummary.adapter = AuditSummaryAdapter(this, mPresenter)


    }

    override fun onClick(v: View?) {

        when (v?.id) {

            R.id.iVProfileHome -> finish()


        }
    }

    override fun onResume() {
        super.onResume()

        showProgress()
        mPresenter.getSummaryList()
    }

    override fun onSuccess() {

    }

    override fun onFailure(error: Throwable?) {
        hideProgress()
        processError(error)
    }

    override fun refreshList() {
        hideProgress()
        binding.rvSummary.adapter?.notifyDataSetChanged()
    }

    override fun takeToSummaryScreen(auditId: Long, startTime: String, endTime: String) {
        val intent = Intent(this@AuditSummaryActivity, AuditSummaryDetailActivity::class.java)
        intent.putExtra(audit_id, auditId)
        intent.putExtra(start_time, startTime)
        intent.putExtra(end_time, endTime)
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        mPresenter.onDetachView()
    }

    override fun showAlert() {

        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.summary_in_progress))
        builder.setMessage(getString(R.string.summary_in_progress_message))
        builder.setPositiveButton(getString(R.string.ok),null)
        builder.show()

    }
}
