package com.goflash.dispatch.features.lastmile.tripCreation.ui.activity

import android.Manifest.permission.CALL_PHONE
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import com.goflash.dispatch.R
import com.goflash.dispatch.SortationApplication
import com.goflash.dispatch.api_services.SessionService
import com.goflash.dispatch.app_constants.FILTER_DEBOUNCE
import com.goflash.dispatch.app_constants.PERMISSION_REQUEST_CODE
import com.goflash.dispatch.data.UnassignedCount
import com.goflash.dispatch.databinding.ActivityLastMileBinding
import com.goflash.dispatch.di.components.DaggerActivityComponents
import com.goflash.dispatch.features.lastmile.tripCreation.listeners.FragmentListener
import com.goflash.dispatch.features.lastmile.tripCreation.presenter.LastMilePresenter
import com.goflash.dispatch.features.lastmile.tripCreation.ui.adapter.LastMileTabAdapter
import com.goflash.dispatch.features.lastmile.tripCreation.ui.fragments.CompletedFragment
import com.goflash.dispatch.features.lastmile.tripCreation.ui.fragments.CreatedFragment
import com.goflash.dispatch.features.lastmile.tripCreation.ui.fragments.OfdFragment
import com.goflash.dispatch.features.lastmile.tripCreation.ui.fragments.ReconFinishFragment
import com.goflash.dispatch.features.lastmile.tripCreation.view.LastMileView
import com.goflash.dispatch.ui.activity.BaseActivity
import com.goflash.dispatch.util.*
import rx.subjects.PublishSubject
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class LastMileActivity : BaseActivity(), LastMileView, ViewPager.OnPageChangeListener,
    View.OnClickListener,
    FragmentListener {

    private val TAG = LastMileActivity::class.java.simpleName

    @Inject
    lateinit var mPresenter: LastMilePresenter

    private var mAdapter: LastMileTabAdapter? = null

    var refreshPage: MutableList<RefreshPage> = mutableListOf()

    private lateinit var subject: PublishSubject<String>

    private var selectedDate1: Long = 0
    private var selectedDate2: Long = 0

    private lateinit var binding: ActivityLastMileBinding

    companion object {
        var createdCount: Int? = 0
        var ofdCount: Int? = 0
        var completedCount: Int? = 0
        var rfCount: Int? = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initDagger()
        binding = ActivityLastMileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //PreferenceHelper.init(this)

        initViews()
        initViewPager()
    }

    private fun initViews() {
        binding.toolBar.toolbarTitle.text = resources.getString(R.string.last_mile)

        if (SessionService.selfAssignment) {
            binding.llBottomMenu.tvAssignZone.isEnabled = false
            binding.llBottomMenu.tvAssignZone.setBackgroundResource(R.drawable.disable_button)

            binding.llBottomMenu.ivCreateManualTrip.isEnabled = false
        } else {
            binding.llBottomMenu.tvAssignZone.isEnabled = true
            binding.llBottomMenu.tvAssignZone.setBackgroundResource(R.drawable.assign_zones_background)

            binding.llBottomMenu.ivCreateManualTrip.isEnabled = true
        }

        binding.llSearchFilter.tvDate.setOnClickListener(this)
        binding.llBottomMenu.tvUnassigned.setOnClickListener(this)
        binding.llBottomMenu.ivCreateManualTrip.setOnClickListener(this)
        binding.llSearchFilter.llDateSelect.setOnClickListener(this)
        binding.llSearchFilter.ivDateClear.setOnClickListener(this)
        binding.toolBar.iVProfileHome.setOnClickListener(this)
        binding.llBottomMenu.tvAssignZone.setOnClickListener(this)
        binding.llBottomMenu.tvCancelTrip.setOnClickListener(this)

        // binding.llBottomMenu.tvAssignZone.text = getString(R.string.create_manual_trip)

        subject = PublishSubject.create()
        subject.debounce(FILTER_DEBOUNCE, TimeUnit.MILLISECONDS)
            .subscribe {
                runOnUiThread {
                    filter(it)
                }
            }

        binding.llSearchFilter.edtSearchDelivery.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable?) {
                if (s?.isNotEmpty() == true) {
                    subject.onNext(s.toString().trim().toLowerCase())
                    binding.llSearchFilter.edtSearchDelivery.setCompoundDrawablesWithIntrinsicBounds(
                        ContextCompat.getDrawable(
                            this@LastMileActivity,
                            R.drawable.ic_search
                        ),
                        null,
                        ContextCompat.getDrawable(
                            this@LastMileActivity,
                            R.drawable.ic_clear_white
                        ),
                        null
                    )
                } else {
                    binding.llSearchFilter.edtSearchDelivery.setCompoundDrawablesWithIntrinsicBounds(
                        ContextCompat.getDrawable(
                            this@LastMileActivity,
                            R.drawable.ic_search
                        ), null, null, null
                    )
                    subject.onNext(s.toString().trim().toLowerCase())
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s != null)
                    subject.onNext(s.toString().trim().toLowerCase())
            }
        })

        binding.llSearchFilter.edtSearchDelivery.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                if (binding.llSearchFilter.edtSearchDelivery.compoundDrawables[2] != null)
                    if (event.rawX >= (binding.llSearchFilter.edtSearchDelivery.right - binding.llSearchFilter.edtSearchDelivery.compoundDrawables[2].bounds.width())) {
                        binding.llSearchFilter.edtSearchDelivery.text.clear()
                        true
                    }
            }
            false
        }

        showProgress()
        mPresenter.getTabCount()

    }

    private fun initDagger() {
        DaggerActivityComponents.builder()
            .networkComponent((application as SortationApplication).getNetworkComponent())
            .build().inject(this@LastMileActivity)

        mPresenter.onAttachView(this, this@LastMileActivity)
    }

    private fun initViewPager() {

        binding.llViewPager.viewPager.addOnPageChangeListener(this)

        mAdapter = LastMileTabAdapter(supportFragmentManager)

        mAdapter?.addFragment(CreatedFragment(), "Created")
        mAdapter?.addFragment(OfdFragment(), "OFD")
        mAdapter?.addFragment(CompletedFragment(), "Completed")
        mAdapter?.addFragment(ReconFinishFragment(), "Recon Finish")

        binding.llViewPager.viewPager.adapter = mAdapter
        binding.llViewPager.viewPager.offscreenPageLimit = 1

        binding.llViewPager.tabLayout.setupWithViewPager(binding.llViewPager.viewPager, true)


        setMargin()
    }

    private fun filter(query: String) {

        if (query.trim().isNotEmpty() && query.trim().length >= 3) {
            if (onlyDigits(query))
                PreferenceHelper.routeId = query
            else
                PreferenceHelper.agentName = query

            updateViewPager()

        } else if (query.trim().isEmpty()) {
            PreferenceHelper.agentName = null
            PreferenceHelper.routeId = null
            updateViewPager()
        }
    }

    override fun onStart() {
        super.onStart()

        PreferenceHelper.startDate = getTimestampString(6)
        PreferenceHelper.endDate = getTimestampString2()

        selectedDate1 = getTimeInMillis(6)
        selectedDate2 = getTimeInMillis(0)

        mAdapter?.getItemPosition("")
        setupDateSelector()

        showProgress()
        mPresenter.getUnassignedCount()

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tvDate -> showDatePicker()
            R.id.tvUnassigned -> startUnassignedShipmentsActivity()
            R.id.ivCreateManualTrip -> startSprinterActivity()
            R.id.tvAssignZone -> startAssignZoneActivity()
            R.id.llDateSelect -> showDatePicker()
            R.id.ivDateClear -> dateClear()
            R.id.iVProfileHome -> finish()
        }
    }

    private fun dateClear() {
        setupDateSelector()
        updateViewPager()
    }

    private fun setupDateSelector() {
        binding.llSearchFilter.llDateSelect.visibility = View.GONE
        binding.llSearchFilter.tvDate.visibility = View.VISIBLE

        PreferenceHelper.startDate = getTimestampString(6)
        PreferenceHelper.endDate = getTimestampString2()
    }

    private fun showDatePicker() {
        val picker = dateRange(selectedDate1, selectedDate2).build()
        picker.show(this.supportFragmentManager, picker.toString())
        picker.addOnNegativeButtonClickListener { }
        picker.addOnPositiveButtonClickListener { updateViewPager(it.first!!, it.second) }
    }

    private fun updateViewPager(date1: Long, date2: Long?) {
        if (date2 == null)
            return

        binding.llSearchFilter.llDateSelect.visibility = View.VISIBLE
        binding.llSearchFilter.tvDate.visibility = View.GONE

        selectedDate1 = date1
        selectedDate2 = date2

        PreferenceHelper.startDate = getDateString(date1.toString())
        PreferenceHelper.endDate = getDateString(date2.toString())

        refreshPage.forEach { it.refreshPage() }

        setMargin()
    }

    override fun onSuccess(unassignedCount: UnassignedCount?) {
        hideProgress()
        binding.llBottomMenu.tvUnassigned.text = unassignedCount?.count.toString()

        updateViewPager()
    }

    override fun onTabCount() {
        hideProgress()
        commonListener()
    }

    override fun onFailure(error: Throwable?) {
        hideProgress()
        processError(error)
    }

    interface RefreshPage {
        fun refreshPage()
    }

    override fun onPageScrollStateChanged(state: Int) {
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

    }

    override fun onPageSelected(position: Int) {
    }

    private fun startUnassignedShipmentsActivity() {
        startActivity(Intent(this, UnassignedShipmentActivity::class.java))

    }

    private fun startSprinterActivity() {
        startActivity(Intent(this, SelectSprinterActivity::class.java))
    }

    private fun startAssignZoneActivity() {
        startActivity(Intent(this, AssignZonesActivity::class.java))
    }

    private fun setMargin() {
        for (i in 0 until binding.llViewPager.tabLayout.tabCount) {
            val tab = (binding.llViewPager.tabLayout.getChildAt(0) as ViewGroup).getChildAt(i)
            val p = tab.layoutParams as ViewGroup.MarginLayoutParams
            p.setMargins(14, 0, 4, 0)
            tab.requestLayout()
        }
    }

    private fun updateViewPager() {
        refreshPage.forEach { it.refreshPage() }
        setMargin()
    }

    override fun deleteOrUnblockShipment(position: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun commonListener() {

        binding.llViewPager.tabLayout.getTabAt(0)?.text = "${mAdapter?.getPageTitle(0)} ($createdCount)"
        binding.llViewPager.tabLayout.getTabAt(1)?.text = "${mAdapter?.getPageTitle(1)} ($ofdCount)"
        binding.llViewPager.tabLayout.getTabAt(2)?.text = "${mAdapter?.getPageTitle(2)} ($completedCount)"
        binding.llViewPager.tabLayout.getTabAt(3)?.text = "${mAdapter?.getPageTitle(3)} ($rfCount)"

        setMargin()
    }

    override fun callDeliveryAgent(phoneNumber: String) {
        if (!checkPermission()) run {
            requestPermission()
        } else
            makeCallToCustomer(phoneNumber)
    }

    private fun makeCallToCustomer(phoneNumber: String) {
        checkPermission()
        val intent = Intent(Intent.ACTION_CALL, Uri.fromParts("tel", phoneNumber, null))
        val chooser = Intent.createChooser(intent, "Complete action using..")
        startActivity(chooser)
    }

    /**
     * Method to check call permission if not it will request at runtime
     */
    private fun checkPermission(): Boolean {
        val result = ContextCompat.checkSelfPermission(applicationContext, CALL_PHONE)
        return result == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Method to request call permission
     */
    private fun requestPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(CALL_PHONE), PERMISSION_REQUEST_CODE)
    }

    private fun showMessageOKCancel(message: String, okListener: (Any, Any) -> Unit) {
        AlertDialog.Builder(this@LastMileActivity)
            .setMessage(message)
            .setPositiveButton(resources.getString(R.string.ok), okListener)
            .setNegativeButton(resources.getString(R.string.cancel), null)
            .create()
            .show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                val callAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED

                if (!callAccepted)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(CALL_PHONE)) {
                            showMessageOKCancel(
                                resources.getString(R.string.mandatory_call_permission)
                            ) { _, _ ->
                                requestPermissions(
                                    arrayOf(CALL_PHONE),
                                    PERMISSION_REQUEST_CODE
                                )
                            }
                            return
                        }
                    }
            }
        }
    }


    override fun setCancelView() {
        binding.llBottomMenu.tvCancelTrip.visibility = View.VISIBLE
        binding.llBottomMenu.tvAssignZone.visibility = View.GONE
        binding.llBottomMenu.ivCreateManualTrip.visibility = View.GONE
    }

    override fun onCancelSuccess() {
        hideProgress()
        binding.llBottomMenu.tvCancelTrip.visibility = View.GONE
        binding.llBottomMenu.tvAssignZone.visibility = View.VISIBLE
        binding.llBottomMenu.ivCreateManualTrip.visibility = View.VISIBLE
    }


    override fun onResume() {
        super.onResume()

        //mPresenter.smartTripProcess()
    }

    override fun onDestroy() {
        super.onDestroy()
        mPresenter.onDetachView()
    }
}
