package com.goflash.dispatch.features.lastmile.tripCreation.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.goflash.dispatch.R
import com.goflash.dispatch.SortationApplication
import com.goflash.dispatch.app_constants.*
import com.goflash.dispatch.data.InTransitTrip
import com.goflash.dispatch.data.SprinterForZone
import com.goflash.dispatch.databinding.LayoutAssignZonesBinding
import com.goflash.dispatch.di.components.DaggerActivityComponents
import com.goflash.dispatch.features.lastmile.tripCreation.listeners.DemergeListener
import com.goflash.dispatch.features.lastmile.tripCreation.listeners.SprinterFragmentListener
import com.goflash.dispatch.features.lastmile.tripCreation.listeners.ZoneItemListener
import com.goflash.dispatch.features.lastmile.tripCreation.presenter.AssignZonePresenter
import com.goflash.dispatch.features.lastmile.tripCreation.ui.adapter.AssignZoneAdapter
import com.goflash.dispatch.features.lastmile.tripCreation.ui.fragments.BottomSheetDemergeZoneFragment
import com.goflash.dispatch.features.lastmile.tripCreation.view.AssignZoneView
import com.goflash.dispatch.model.ZoneSprinterDTO
import com.goflash.dispatch.ui.activity.BaseActivity
import com.goflash.dispatch.ui.itemDecoration.MarginItemDecoration
import com.goflash.dispatch.util.getTimestampStringForMonth
import javax.inject.Inject

class AssignZonesActivity : BaseActivity(), AssignZoneView, View.OnClickListener, ZoneItemListener,
    DemergeListener, SprinterFragmentListener {

    @Inject
    lateinit var mPresenter: AssignZonePresenter

    private var assignZoneAdapter: AssignZoneAdapter? = null

    private var mergeZones = mutableListOf<ZoneSprinterDTO>()

    private var fragment: SelectSprinterForZoneFragment? = null

    private var merge = false

    private var getEDD: String? = null
    private var type: String? = null
    private var tag: String? = null
    private var serviceType: String? = null
    private var paymentStatus: String? = null
    private var eddSelected = 3
    private var tagSelected = 0
    private var eddStart: String? = null
    private var eddEnd: String? = null

    private var mergeAllowed = true

    private var excludedTrips: MutableList<String> = mutableListOf()

    private lateinit var binding: LayoutAssignZonesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LayoutAssignZonesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initDagger()
        initViews()
    }

    private fun initDagger() {
        DaggerActivityComponents.builder()
            .networkComponent((application as SortationApplication).getNetworkComponent())
            .build().inject(this)

        mPresenter.onAttachView(this, this)
    }

    private fun initViews() {

        binding.btnCreate.btnPayment.text = getString(R.string.create_trips)
        binding.btnCreate.btnPayment.isEnabled = false
        binding.btnCreate.btnPayment.setBackgroundResource(R.drawable.grey_button_background)

        binding.btnMerge.btnSave.text = getString(R.string.merge_zones)
        binding.btnMerge.btnClear.text = getString(R.string.cancel_caps)

        binding.rvZones.layoutManager = LinearLayoutManager(this)

        binding.rvZones.addItemDecoration(
            MarginItemDecoration(
                resources.getDimension(R.dimen.margin_12).toInt()
            )
        )

        binding.layoutFilter.edtSearchPincode.hint = "Search Zone"
        binding.layoutFilter.edtSearchPincode.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s?.isNotEmpty() == true) {
                    mPresenter.applyZoneFilter(s.toString())
                    binding.layoutFilter.edtSearchPincode.setCompoundDrawablesWithIntrinsicBounds(
                        ContextCompat.getDrawable(
                            this@AssignZonesActivity,
                            R.drawable.search_white
                        ),
                        null,
                        ContextCompat.getDrawable(
                            this@AssignZonesActivity,
                            R.drawable.ic_clear_white
                        ),
                        null
                    )
                } else {
                    binding.layoutFilter.edtSearchPincode.setCompoundDrawablesWithIntrinsicBounds(
                        ContextCompat.getDrawable(
                            this@AssignZonesActivity,
                            R.drawable.search_white
                        ), null, null, null
                    )
                    mPresenter.clearFilter()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })

        binding.layoutFilter.edtSearchPincode.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                if (binding.layoutFilter.edtSearchPincode.compoundDrawables[2] != null)
                    if (event.rawX >= (binding.layoutFilter.edtSearchPincode.right - binding.layoutFilter.edtSearchPincode.compoundDrawables[2].bounds.width())) {
                        binding.layoutFilter.edtSearchPincode.text.clear()
                        true
                    }
            }
            false
        }
        binding.toolBar.ivMerge.setOnClickListener(this)
        binding.btnMerge.btnClear.setOnClickListener(this)
        binding.btnMerge.btnSave.setOnClickListener(this)
        binding.btnCreate.btnPayment.setOnClickListener(this)
        binding.toolBar.iVProfileHome.setOnClickListener(this)
        binding.layoutFilter.tvFilter.setOnClickListener(this)

        binding.tvChange.setOnClickListener(this)

        showProgress()

        mPresenter.getShipments(null, null, null,excludedTrips,tag,serviceType,paymentStatus)
    }

    override fun displayProgress() {
        showProgress()
    }


    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.ivMerge -> {
                if (mergeAllowed) {
                    merge = true
                    assignZoneAdapter?.showOrHideCheckbox(true)
                    binding.btnCreate.flSave.visibility = View.GONE
                    binding.btnMerge.flSave.visibility = View.VISIBLE
                }
            }
            R.id.btn_clear -> {
                merge = false
                assignZoneAdapter?.showOrHideCheckbox(false)
                binding.btnCreate.flSave.visibility = View.VISIBLE
                binding.btnMerge.flSave.visibility = View.GONE
            }
            R.id.btn_save -> {
                if(mergeZones.isNotEmpty()) {
                    merge = false
                    mPresenter.mergeZones(mergeZones)
                }
            }
            R.id.btn_payment -> {
                showProgress()
                mPresenter.createTrips()
            }
            R.id.tvFilter -> startFilterActivity()
            R.id.iVProfileHome -> finish()
            R.id.tvChange -> startInTransitActivity()

        }
    }

    private fun startInTransitActivity() {
        val trips = mPresenter.getInTransitTrips()
        val intent = Intent(this, InTransitTripsActivity::class.java)
        intent.putExtra("trips", trips)
        startActivityForResult(intent, INTRANSIT_TRIP)
    }

    override fun onResume() {
        super.onResume()

        if(mergeZones.isNotEmpty()) {
            binding.btnCreate.flSave.visibility = View.GONE
            binding.btnMerge.flSave.visibility = View.VISIBLE
        }
    }

    private fun startFilterActivity() {
        mPresenter.clearSprinters()

        val intent = Intent(this, FilterActivity::class.java)
        intent.putExtra(TASKTYPE, type)
        intent.putExtra(EDD, eddSelected)
        intent.putExtra(TAGS,tagSelected)
        intent.putExtra("smartTrips", true)
        intent.putExtra(SERVICETYPE,serviceType)
        intent.putExtra(PAYMENT_STATUS,paymentStatus)
        startActivityForResult(intent, SUCCESS_CODE)
    }

    override fun setFlag(b: Boolean) {
        merge = b
    }

    override fun addToMergeList(zone: ZoneSprinterDTO) {
        mergeZones.add(zone)

    }

    override fun removeFromMergeList(zone: ZoneSprinterDTO) {
        mergeZones.remove(zone)
    }

    override fun deMergeZones(zone: ZoneSprinterDTO) {
        val bottomSheet = BottomSheetDemergeZoneFragment()
        val bundle = Bundle()
        bundle.putInt("zone", zone.zoneList[0].id)
        var zoneName = ""
        for (i in 0 until zone.zoneList.size) {
            zoneName += if (i != zone.zoneList.size - 1)
                zone.zoneList[i].zoneName + ", "
            else
                zone.zoneList[i].zoneName
        }

        bundle.putString("zoneName", zoneName)
        bottomSheet.arguments = bundle
        bottomSheet.show(supportFragmentManager, bottomSheet.tag)

    }

    override fun removeSprinter(zoneId: Int, sprinter: SprinterForZone) {
        mPresenter.removeSprinterForZone(zoneId, sprinter)
    }

    override fun onConfirm(zone: Int) {
        mPresenter.deMergeZone(zone)
    }

    override fun onItemSelected(position: Int) {
        mPresenter.onItemSelected(position)
    }

    override fun onCancelTrip(tripProcessId: Int) {
        showProgress()
        mPresenter.cancelTripForZone(tripProcessId)
    }


    override fun startSprinterActivity(zoneId: Int, sprinters: MutableList<SprinterForZone>, minSprinterCount: Int) {
        fragment = SelectSprinterForZoneFragment()
        val args = Bundle()
        args.putInt("zone", zoneId)
        args.putInt("minSprinterCount", minSprinterCount)
        args.putParcelableArrayList("sprinters", ArrayList(sprinters))
        fragment?.arguments = args
        supportFragmentManager.beginTransaction().replace(
            R.id.container,
            fragment!!,
            SelectSprinterForZoneFragment::class.java.simpleName
        ).addToBackStack(null).commit()
    }

    override fun setSprinterData(zone: Int, sprinters: List<SprinterForZone>) {
        removeFragment()
        mPresenter.setSprinterForZone(zone, sprinters)
    }

    override fun removeFragment() {
        supportFragmentManager.beginTransaction().remove(fragment!!).commit()
    }

    override fun onFailure(error: Throwable?) {
        hideProgress()
        processError(error)
    }

    override fun onShipmentsFetched(shipments: MutableList<ZoneSprinterDTO>) {
        hideProgress()
        mergeZones.clear()
        binding.btnCreate.flSave.visibility = View.VISIBLE
        binding.btnMerge.flSave.visibility = View.GONE

        mergeAllowed = shipments.isNotEmpty() && shipments.size > 1


        assignZoneAdapter = AssignZoneAdapter(this, shipments, this)
        binding.rvZones.adapter = assignZoneAdapter

        assignZoneAdapter?.showOrHideCheckbox(merge)

        enableOrDisableCreate(shipments.any { it.sprinterList.size > 0 })
    }

    private fun enableOrDisableCreate(enable: Boolean) {
        binding.btnCreate.btnPayment.isEnabled = enable
        if (enable)
            binding.btnCreate.btnPayment.setBackgroundResource(R.drawable.blue_button_background)
        else
            binding.btnCreate.btnPayment.setBackgroundResource(R.drawable.grey_button_background)
    }

    override fun onCreateSuccess() {
        hideProgress()

        val intent = Intent(this, LastMileActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()

        mPresenter.clearSprinters()
        mPresenter.onDetachView()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        showProgress()
        if(requestCode == SUCCESS_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                type = data?.getStringExtra("type")
                getEDD = data?.getStringExtra("getEDD")
                tag = data?.getStringExtra("tag")
                eddSelected = data?.getIntExtra("selectedEDD", -1) ?: -1
                tagSelected = data?.getIntExtra("selectedTag", -1) ?: -1
                serviceType = data?.getStringExtra(SERVICETYPE)
                paymentStatus = data?.getStringExtra(PAYMENT_STATUS)

                when {
                    eddSelected == 1 -> {
                        eddStart = getTimestampStringForMonth(2)
                        eddEnd = getEDD
                    }
                    eddSelected != 3 -> {
                        eddStart = getTimestampStringForMonth(2)
                        eddEnd = getEDD
                    }
                    else -> {
                        eddStart = null
                        eddEnd = null
                    }
                }
                mPresenter.getShipments(type, eddStart, eddEnd, excludedTrips,tag,serviceType,paymentStatus)
            } else if (resultCode == Activity.RESULT_CANCELED) {
                hideProgress()
            } else
                mPresenter.getShipments(null, null, null, excludedTrips,tag,serviceType,paymentStatus) //need to check this line
        }else if(requestCode == INTRANSIT_TRIP){
            if (resultCode == Activity.RESULT_OK) {
                val trips = data?.getSerializableExtra("tripsSelected") as ArrayList<InTransitTrip>
                mPresenter.setInTransitTrips(trips)
                excludedTrips.clear()
                for(trip in trips)
                    if(!trip.selected)
                        excludedTrips.add(trip.id.toString())
                binding.tvIncludes.text = String.format(getString(R.string.includes_in_transit), trips.filter { it.selected }.size)

            }

            mPresenter.getShipments(type, eddStart, eddEnd,  excludedTrips,tag,serviceType,paymentStatus)
        }
    }

    override fun showIntransitCount(count: Int) {
       // hideProgress()
        if (count > 0) {
            binding.clInTransit.visibility = View.VISIBLE
            binding.tvIncludes.text = String.format(getString(R.string.includes_in_transit), count)
        } else {
            binding.clInTransit.visibility = View.GONE
        }
    }

    override fun onCancelSuccess() {
        hideProgress()
        showToast(this, "Existing process is cancelled successfully")
        mPresenter.getShipments(null, null, null,excludedTrips,tag,serviceType,paymentStatus)
    }
}