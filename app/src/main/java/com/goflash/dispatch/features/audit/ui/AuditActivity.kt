package com.goflash.dispatch.features.audit.ui

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.goflash.dispatch.R
import com.goflash.dispatch.SortationApplication
import com.goflash.dispatch.api_services.SessionService
import com.goflash.dispatch.app_constants.audit_id
import com.goflash.dispatch.app_constants.bag_count
import com.goflash.dispatch.app_constants.shipment_count
import com.goflash.dispatch.databinding.ActivityAuditBinding
import com.goflash.dispatch.di.components.DaggerActivityComponents
import com.goflash.dispatch.features.audit.presenter.AuditPresenter
import com.goflash.dispatch.features.audit.view.AuditView
import com.goflash.dispatch.ui.activity.BaseActivity
import com.goflash.dispatch.util.PreferenceHelper
import javax.inject.Inject

class AuditActivity : BaseActivity(), View.OnClickListener,
    AuditView {

    @Inject
    lateinit var mPresenter: AuditPresenter

    lateinit var binding: ActivityAuditBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        initDagger()
    }

    private fun initDagger() {
        DaggerActivityComponents.builder().networkComponent((application as SortationApplication).getNetworkComponent())
            .build().inject(this@AuditActivity)

        mPresenter.onAttachView(this, this@AuditActivity)
    }

    private fun initViews(){

        binding.toolBar1.toolbarTitle.text = String.format(getString(R.string.audit_for),
            PreferenceHelper.assignedAssetName)

        binding.toolBar1.ivHistory.visibility = View.VISIBLE

        binding.userName.text = String.format(getString(R.string.hi_name), SessionService.name)

        binding.btnAudit.setOnClickListener(this)
        binding.toolBar1.iVProfileHome.setOnClickListener(this)
        binding.toolBar1.ivHistory.setOnClickListener(this)
    }

    override fun onClick(v: View?) {

        when(v?.id){

            R.id.btn_audit ->  message()

            R.id.iVProfileHome -> finish()

            R.id.ivHistory -> showSummaryActiivty()

        }

    }

    private fun showAuditScanAcitvity(bagCount : Long, shipmentCount : Long, auditId : Long){
        val intent = Intent(this, AuditScanActivity::class.java)
        intent.putExtra(bag_count,bagCount)
        intent.putExtra(shipment_count,shipmentCount)
        intent.putExtra(audit_id,auditId)
        startActivity(intent)
    }

    override fun onSuccess(bagCount : Long, shipmentCount : Long, auditId : Long) {
        hideProgress()
        showAuditScanAcitvity(bagCount,shipmentCount,auditId)

    }

    override fun onFailure(error: Throwable?) {
        hideProgress()

    }

    override fun showActiveAuditData(userName: String, createdTime: String) {
        binding.btnAudit.text = getString(R.string.continue_audit)
        binding.textView3.visibility = View.GONE
        binding.tvauditInProgress.visibility = View.VISIBLE
        binding.createdUser.visibility = View.VISIBLE
        binding.tvcreatedTime.visibility = View.VISIBLE

        val uname = SpannableStringBuilder(String.format(getString(R.string.audit_username),userName))
        uname.setSpan(StyleSpan(Typeface.BOLD),0,8,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.createdUser.text = uname

        val cTime = SpannableStringBuilder(String.format(getString(R.string.created_time),createdTime))
        cTime.setSpan(StyleSpan(Typeface.BOLD),0,12,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.tvcreatedTime.text = cTime

    }

    private fun showSummaryActiivty(){
        val intent = Intent(this, AuditSummaryActivity::class.java)
        startActivity(intent)
    }

    private fun message() {
        if(SessionService.auditActive) {
            startAudit()
            return
        }
        val builder = AlertDialog.Builder(this)
            .setTitle(getString(R.string.start_audit))
            .setMessage(getString(R.string.start_audit_confirm))
            .setCancelable(false)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.confirm) { _, _ -> startAudit() }

        builder.create().show()
    }

    private fun startAudit(){
        showProgress()
        mPresenter.beginAudit()
    }

    override fun onDestroy() {
        super.onDestroy()
        mPresenter.onDetachView()
    }
}
