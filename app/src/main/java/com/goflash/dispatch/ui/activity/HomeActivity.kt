package com.goflash.dispatch.ui.activity

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.Toolbar
import com.goflash.dispatch.BuildConfig
import com.goflash.dispatch.R
import com.goflash.dispatch.SortationApplication
import com.goflash.dispatch.api_services.SessionService
import com.goflash.dispatch.app_constants.isDispatchable
import com.goflash.dispatch.app_constants.run_id
import com.goflash.dispatch.app_constants.show_completed
import com.goflash.dispatch.data.PackageDto
import com.goflash.dispatch.databinding.ActivityHomeBinding
import com.goflash.dispatch.di.components.DaggerActivityComponents
import com.goflash.dispatch.features.audit.ui.AuditActivity
import com.goflash.dispatch.features.bagging.ui.activity.BagListActivity
import com.goflash.dispatch.features.bagging.ui.activity.SortActivity
import com.goflash.dispatch.features.cash.ui.activity.CreateSummaryActivity
import com.goflash.dispatch.features.cash.ui.activity.CashClosingActivity
import com.goflash.dispatch.features.dispatch.ui.activity.*
import com.goflash.dispatch.features.lastmile.tripCreation.ui.activity.LastMileActivity
import com.goflash.dispatch.features.receiving.ui.ReceivingActivity
import com.goflash.dispatch.features.rtoReceiving.ui.activity.ReceiveShipmentsListActivity
import com.goflash.dispatch.features.rtoReceiving.ui.activity.ScanReceiveShipmentActivity
import com.goflash.dispatch.model.*
import com.goflash.dispatch.presenter.HomePresenter
import com.goflash.dispatch.presenter.views.HomeView
import com.goflash.dispatch.util.PreferenceHelper
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class HomeActivity : BaseActivity(), View.OnClickListener, HomeView {

    @Inject
    lateinit var homePresenter: HomePresenter

    private lateinit var remoteConfig: FirebaseRemoteConfig

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        PreferenceHelper.init(this)
        initViews()
        initDagger()



        remoteConfig = FirebaseRemoteConfig.getInstance()

        val configSettings = FirebaseRemoteConfigSettings.Builder().setMinimumFetchIntervalInSeconds(if(BuildConfig.DEBUG) 0 else 3600).build()
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)

        fetch()

    }

    private fun fetch() {
        val fetch = remoteConfig.fetch(if (BuildConfig.DEBUG) 0 else TimeUnit.HOURS.toSeconds(12))
        fetch.addOnCompleteListener {
            if (it.isSuccessful) {
                remoteConfig.activate()
                fetchDataForNumDays()
                Log.d("RemoteConfig", "Fetch and activate succeeded")

            } else {
                Log.d("RemoteConfig", "Fetch failed")
            }
        }
    }

    private fun initViews() {
        showRoles()

        binding.taskActionLayout.llSort.setOnClickListener(this)
        binding.taskLayout.tvSortAndBagCount.setOnClickListener(this)
        binding.taskActionLayout.llReceive.setOnClickListener(this)
        binding.taskActionLayout.llReceiveShipments.setOnClickListener(this)
        binding.taskActionLayout.llDispatch.setOnClickListener(this)
        binding.tvInvoiceList.setOnClickListener(this)
        binding.tvRunsheetList.setOnClickListener(this)
        binding.taskLayout.tvDispatchedCount.setOnClickListener(this)
        binding.taskLayout.tvReceivedCount.setOnClickListener(this)
        binding.taskActionLayout.llAudit.setOnClickListener(this)
        binding.toolBar1.ivRaiseTicket.setOnClickListener(this)

        binding.taskActionLayout.llLastMile.setOnClickListener(this)
        binding.taskActionLayout.llCashClosing.setOnClickListener(this)


        val toolbar = findViewById<View>(R.id.toolBar1) as Toolbar
        setSupportActionBar(toolbar)

        binding.toolBar1.toolbarTitle.text = PreferenceHelper.assignedAssetName //getString(R.string.pharmeasy)

        val actionbar: ActionBar? = supportActionBar
        actionbar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_profile_tab_ico)
            setDisplayShowTitleEnabled(false)
        }

        binding.clInvoice.visibility = if(PreferenceHelper.invoiceGenerationFlag) View.VISIBLE else View.GONE
    }

    private fun initDagger() {
        DaggerActivityComponents.builder()
            .networkComponent((application as SortationApplication).getNetworkComponent())
            .build().inject(this@HomeActivity)

        homePresenter.onAttachView(this, this@HomeActivity)
    }


    override fun onClick(view: View?) {
        when (view!!.id) {
            R.id.ll_sort -> startSortActivity()
            R.id.tv_sort_and_bag_count -> startBagListActivity()
            R.id.ll_receive -> startReceivingActivity(false)
            R.id.ll_dispatch -> {
                if (!isAuditActive())
                    homePresenter.checkIfDispatchStarted()
            }
            R.id.tv_invoice_list -> startInvoiceListActivity()
            R.id.tv_dispatched_count -> startTripListingActivity()
            R.id.tv_received_count -> startReceivingActivity(true)
            R.id.ll_Audit -> startAuditActivity()
            R.id.ivRaiseTicket -> showRaiseTicketActivity()
            R.id.ll_cash_closing -> startCashClosingActivity()
            R.id.ll_lastMile -> {
                if (!isAuditActive())
                    startLastMileActivity()
            }
            R.id.tv_cash -> {
                if (!isAuditActive())
                    startCashClosingActivity()
            }
            R.id.ll_receiveShipments -> {
                showProgress()
                homePresenter.getInwardRuns()
            }
            R.id.tvRunsheetList -> startRunsheetListActivity()
        }
    }

    private fun fetchDataForNumDays(){
        PreferenceHelper.dataForNumDays = remoteConfig.getLong("dataForNumOfDays").toInt()

        Log.d("RemoteConfig", "Pref ${PreferenceHelper.dataForNumDays}")


    }

    private fun showRaiseTicketActivity() {
        val intent = Intent(this@HomeActivity, RaiseTicketActivity::class.java)
        startActivity(intent)
    }


    private fun startReceivingActivity(showCompleted: Boolean) {
        if (!isAuditActive()) {
            val intent = Intent(this@HomeActivity, ReceivingActivity::class.java)
            intent.putExtra(show_completed, showCompleted)
            startActivity(intent)
        }
    }

    private fun startSortActivity() {
        if (!isAuditActive())
            startActivity(Intent(this@HomeActivity, SortActivity::class.java))
    }

    private fun startBagListActivity() {
        if (!isAuditActive())
            startActivity(Intent(this@HomeActivity, BagListActivity::class.java))
    }

    private fun startInvoiceListActivity() {
        if (!isAuditActive())
            startActivity(Intent(this@HomeActivity, InvoiceListActivity::class.java))
    }

    private fun startRunsheetListActivity(){
        if (!isAuditActive())
            startActivity(Intent(this@HomeActivity, RunsheetListActivity::class.java))
    }

    private fun startTripListingActivity() {
        if (!isAuditActive())
            startActivity(Intent(this@HomeActivity, TripListActivity::class.java))
    }

    private fun startAuditActivity() {
        startActivity(Intent(this@HomeActivity, AuditActivity::class.java))
    }

    private fun startLastMileActivity() {
        startActivity(Intent(this@HomeActivity, LastMileActivity::class.java))
    }


    private fun startCashClosingActivity() {
        startActivity(Intent(this@HomeActivity, CashClosingActivity::class.java))
    }


    override fun onResume() {
        super.onResume()
        showProgress()
        homePresenter.getSummary()
    }

    override fun onDestroy() {
        super.onDestroy()
        homePresenter.onDetachView()
    }

    override fun showBaggedCount(bags: Int, shipments: Int) {
        hideProgress()
        binding.taskLayout.tvSortAndBagCount.text =
            String.format(getString(R.string.sort_and_bag_count, bags, shipments))
    }

    override fun showDispatchedCount(trips: Int) {
        binding.taskLayout.tvDispatchedCount.text = String.format(getString(R.string.dispatched_count, trips))
    }

    override fun showReceivedCount(bags: Int, shipments: Int) {
        binding.taskLayout.tvReceivedCount.text = String.format(getString(R.string.received_count, bags, shipments))
    }

    override fun onFailure(error: Throwable?) {
        hideProgress()
        processError(error)
    }

    override fun takeToCancelledScreen() {
        startActivity(Intent(this@HomeActivity, CancelledActivity::class.java))
    }


    override fun takeToDispatchBinScreen(packageDto: PackageDto, isPackageDispatchable: Boolean) {
        val intent = Intent(this, ScanDispatchBinActivity::class.java)
        intent.putExtra(isDispatchable, isPackageDispatchable)
        startActivity(intent)
    }

    override fun takeToDispatchBagScreen() {
        val intent = Intent(this, DispatchBagActivity::class.java)
        startActivity(intent)
    }

    override fun takeToDispatchScreen() {
        val intent = Intent(this, DispatchActivity::class.java)
        startActivity(intent)

    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                startActivity(Intent(this@HomeActivity, ProfileActivity::class.java))
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun isAuditActive(): Boolean {
        return if (SessionService.auditActive) {
            showALertDialog()
            true
        } else
            false
    }

    private fun showALertDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.audit_active_title))
        builder.setMessage(getString(R.string.audit_active_message))
        builder.setPositiveButton(getString(R.string.ok), null)
        // builder.setOnCancelListener { dialog -> finish() }
        builder.show()
    }

    private fun showRoles() {

        if (SessionService.roles.contains(super_role) || SessionService.roles.contains(store_role) || SessionService.roles.contains(
                dispatcher_role
            )
        ) {

            binding.taskActionLayout.llSort.visibility = View.VISIBLE
            binding.taskActionLayout.llDispatch.visibility = View.VISIBLE
            binding.taskActionLayout.llReceive.visibility = View.VISIBLE
            binding.taskActionLayout.llReceiveShipments.visibility = View.VISIBLE
            binding.taskActionLayout.llLastMile.visibility = View.VISIBLE
            binding.taskActionLayout.llCashClosing.visibility = View.VISIBLE
            binding.taskActionLayout.llAudit.visibility = View.VISIBLE
            binding.taskActionLayout.view1.visibility = View.VISIBLE
            binding.taskActionLayout.view2.visibility = View.VISIBLE
            binding.taskActionLayout. view3.visibility = View.VISIBLE
            binding.taskActionLayout.view4.visibility = View.VISIBLE
            binding.taskActionLayout.view5.visibility = View.VISIBLE
            binding.taskActionLayout.viewReceiveShipments.visibility = View.VISIBLE

            return
        }

        if (SessionService.roles.contains(wh_dispatcher_role)) {
            binding.taskActionLayout.llSort.visibility = View.VISIBLE
            binding.taskActionLayout.llDispatch.visibility = View.VISIBLE
            binding.taskActionLayout.llReceive.visibility = View.VISIBLE
            binding.taskActionLayout.llReceiveShipments.visibility = View.VISIBLE
            binding.taskActionLayout.view1.visibility = View.VISIBLE
            binding.taskActionLayout.view2.visibility = View.VISIBLE
            binding.taskActionLayout.viewReceiveShipments.visibility = View.VISIBLE


            return
        }
    }

    override fun takeToReceiveScanScreen(runId: Int) {
        hideProgress()
        val intent = Intent(this, ScanReceiveShipmentActivity::class.java)
        intent.putExtra(run_id, runId)
        startActivity(intent)
    }

    override fun takeToReceivingListScreen(disableComplete: Boolean) {
        hideProgress()
        val intent = Intent(this, ReceiveShipmentsListActivity::class.java)
        intent.putExtra("disableComplete", disableComplete)
        startActivity(intent)
    }

}