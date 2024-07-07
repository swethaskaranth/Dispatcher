package com.goflash.dispatch.features.receiving.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.ActionBar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.appcompat.widget.Toolbar
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import co.uk.rushorm.core.RushCore
import co.uk.rushorm.core.RushSearch
import com.goflash.dispatch.R
import com.goflash.dispatch.SortationApplication
import com.goflash.dispatch.app_constants.show_completed
import com.goflash.dispatch.data.VehicleDetails
import com.goflash.dispatch.databinding.ActivitySummaryBinding
import com.goflash.dispatch.di.components.DaggerActivityComponents
import com.goflash.dispatch.features.receiving.adapter.ExcessBagAdapter
import com.goflash.dispatch.features.receiving.adapter.ScannedBagAdapter
import com.goflash.dispatch.features.receiving.adapter.ShortBagAdapter
import com.goflash.dispatch.features.receiving.presenter.SummaryPresenter
import com.goflash.dispatch.features.receiving.view.SummaryView
import com.goflash.dispatch.listeners.OnSpinnerItemSelected
import com.goflash.dispatch.model.BagDetails
import com.goflash.dispatch.ui.activity.BaseActivity
import com.goflash.dispatch.ui.activity.HomeActivity
import com.goflash.dispatch.util.BAGID
import com.goflash.dispatch.util.TRIPID
import com.goflash.dispatch.util.VEHICLEID
import javax.inject.Inject

class SummaryActivity : BaseActivity(), SummaryView, View.OnClickListener, OnSpinnerItemSelected {

    @Inject
    lateinit var mPresenter: SummaryPresenter

    private var items = mutableListOf<VehicleDetails>()
    private var shortItems = mutableListOf<VehicleDetails>()
    private var excessItems = mutableListOf<VehicleDetails>()
    private var scannedItems = mutableListOf<VehicleDetails>()

    private var vehicleId: String? = null

    private var tripId : String? = null

    private lateinit var binding: ActivitySummaryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySummaryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initDagger()
        initViews()
    }

    private fun initViews() {
        binding.layoutSummary.btnAddbag.setOnClickListener(this)
        binding.layoutSummary.proceedBtn.setOnClickListener(this)

        val toolbar = findViewById<View>(R.id.toolBar1) as Toolbar
        setSupportActionBar(toolbar)

        val actionbar: ActionBar? = supportActionBar
        actionbar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_back_button_circular)
            setDisplayShowTitleEnabled(false)
        }

        binding.toolBar1.toolbarTitle.text = getString(R.string.summary)

        isEnabled(false)

        vehicleId = intent.getStringExtra(VEHICLEID)
        tripId = intent.getStringExtra(TRIPID)

        binding.layoutSummary.incShort.rvShort.layoutManager = LinearLayoutManager(this)
        binding.layoutSummary.incShort.rvShort.adapter = ShortBagAdapter(this, shortItems, this)

        binding.layoutSummary.incExcess.rvExcess.layoutManager = LinearLayoutManager(this)
        binding.layoutSummary.incExcess.rvExcess.adapter = ExcessBagAdapter(this, excessItems)

        binding.layoutSummary.incScanned.rvScanned.layoutManager = LinearLayoutManager(this)
        binding.layoutSummary.incScanned.rvScanned.adapter = ScannedBagAdapter(this, scannedItems)
    }

    private fun initDagger() {
        DaggerActivityComponents.builder().networkComponent((application as SortationApplication).getNetworkComponent())
                .build().inject(this@SummaryActivity)

        mPresenter.onAttachView(this, this)

        mPresenter.onIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        mPresenter.onTaskResume()
    }

    override fun onClick(v: View?) {
        when (v?.id) {

            R.id.btn_addbag -> showAddBagActivity()

            R.id.proceed_btn -> {
                binding.layoutSummary.proceedBtn.isEnabled = false
                mPresenter.onCompleteTask()}
        }
    }

    override fun onItemSelected(position: Int, spinnerPosition: Int, reason: MutableList<String>, lot: VehicleDetails) {

        if (reason[spinnerPosition] == getString(R.string.select_reason)) {
            showToast(this, getString(R.string.please_select))
            isEnabled(false)
            return
        }

        val vDetails = if(lot.vehicleId != null)
            RushSearch().whereEqual(VEHICLEID, lot.vehicleId).and().whereEqual(BAGID, lot.bagId).findSingle(
            VehicleDetails::class.java)
        else
            RushSearch().whereEqual(TRIPID, lot.tripId).and().whereEqual(BAGID, lot.bagId).findSingle(
                VehicleDetails::class.java)
        vDetails.returnReason = reason[spinnerPosition]
        vDetails.save()

        checkIfReasonIsNull()
    }


    private fun checkIfReasonIsNull() {
        if (scannedItems.any { it.returnReason == null })
            return
        else
            mPresenter.onTaskResume()
    }

    override fun updateViews(bagDetails: MutableList<VehicleDetails>) {

        items.clear()
        shortItems.clear()
        excessItems.clear()
        scannedItems.clear()

        items.addAll(bagDetails)
        shortItems.addAll(items.filter { it.canBePicked && !it.isScanned }.sortedBy { it.bagId })
        excessItems.addAll(items.filter { !it.canBePicked && it.isScanned })
        scannedItems.addAll(items.filter { it.canBePicked && it.isScanned })

        if (shortItems.isEmpty() || shortItems.any { !it.returnReason.isNullOrBlank() }) {
            isEnabled(true)
        }

        if (shortItems.isEmpty())
            binding.layoutSummary.incShort.incShort.visibility = View.GONE
        else
            binding.layoutSummary.incShort.incShort.visibility = View.VISIBLE

        if (excessItems.isNotEmpty()) {
            binding.layoutSummary.incExcess.incExcess.visibility = View.VISIBLE
        }

        if (bagDetails.any { !it.canBePicked && !it.isScanned }) {
            binding.layoutSummary.proceedBtn.text = getString(R.string.proceed)
        }

    }

    override fun enableButton() {
        binding.layoutSummary.proceedBtn.text = getString(R.string.proceed)

        isEnabled(true)

        showProceedActivity()
    }

    override fun takeToReceivingActivity() {
        showToast(this,"Bags cannot be empty. Please try receiving the bags again.")
        val intent = Intent(this,ReceivingActivity::class.java)
        intent.putExtra(show_completed, false)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
    }

    private fun isEnabled(boolean: Boolean) {

        if (boolean) {
            binding.layoutSummary.btnAddbag.isEnabled = true
            binding.layoutSummary.proceedBtn.isEnabled = true

            binding.layoutSummary.btnAddbag.setBackgroundResource(R.color.colorPrimary)
            binding.layoutSummary.proceedBtn.setBackgroundResource(R.color.md_orange_800)
            return
        }

        binding.layoutSummary.btnAddbag.isEnabled = false
        binding.layoutSummary.proceedBtn.isEnabled = false

        binding.layoutSummary.btnAddbag.setBackgroundResource(R.color.border_grey)
        binding.layoutSummary.proceedBtn.setBackgroundResource(R.color.border_grey)
    }

    override fun onFailure(error: Throwable?) {
        hideProgress()
        if (error != null)
            processError(error)
        binding.layoutSummary.proceedBtn.isEnabled = true
    }

    override fun onSuccess() {
        hideProgress()

        showToast(this, getString(R.string.task_completed))
        showHomeActivity()
    }


    private fun showAddBagActivity() {
        val intent = Intent(this, AddBagsActivity::class.java)
        intent.putExtra(VEHICLEID, vehicleId)
        intent.putExtra(TRIPID,tripId)
        startActivity(intent)
    }

    private fun showProceedActivity() {
        val intent = Intent(this, CompleteActivity::class.java)
        intent.putExtra(VEHICLEID, vehicleId)
        intent.putExtra(TRIPID,tripId)
        startActivity(intent)
    }

    private fun showHomeActivity() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finishAffinity()
    }

    override fun onShowProgress() {
        showProgress()
    }

    override fun onHideProgress() {
        hideProgress()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun showErrorAndRedirect(message: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.trip_completed))
        builder.setMessage(message)
        builder.setPositiveButton(getString(R.string.ok)) { i1, i2 ->

            val intent = Intent(this@SummaryActivity,ReceivingActivity::class.java)
            intent.putExtra(show_completed, false)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }
        builder.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        mPresenter.onDetachView()
    }
}
