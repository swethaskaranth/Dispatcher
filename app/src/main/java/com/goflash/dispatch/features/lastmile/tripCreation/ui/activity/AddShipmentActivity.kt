package com.goflash.dispatch.features.lastmile.tripCreation.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.goflash.dispatch.R
import com.goflash.dispatch.SortationApplication
import com.goflash.dispatch.app_constants.*
import com.goflash.dispatch.data.InTransitTrip
import com.goflash.dispatch.data.UnassignedDTO
import com.goflash.dispatch.databinding.ActivityAddShipmentBinding
import com.goflash.dispatch.di.components.DaggerActivityComponents
import com.goflash.dispatch.features.lastmile.tripCreation.listeners.OnShipmentSelectedListener
import com.goflash.dispatch.features.lastmile.tripCreation.presenter.AddShipmentPresenter
import com.goflash.dispatch.features.lastmile.tripCreation.ui.adapter.AddShipmentAdapter
import com.goflash.dispatch.features.lastmile.tripCreation.view.AddShipmentView
import com.goflash.dispatch.model.AddressDTO
import com.goflash.dispatch.model.CommonRequest
import com.goflash.dispatch.type.ShipmentType
import com.goflash.dispatch.ui.activity.BaseActivity
import com.goflash.dispatch.util.getTimestampStringForMonth
import javax.inject.Inject

class AddShipmentActivity : BaseActivity(), AddShipmentView, View.OnClickListener,
    OnShipmentSelectedListener {

    private val TAG = AddShipmentActivity::class.java.simpleName

    @Inject
    lateinit var mPresenter: AddShipmentPresenter

    private val selectedShipments: MutableSet<UnassignedDTO> = mutableSetOf()

    private var tripId: Long? = null

    private var getEDD: String? = null
    private var tag: String? = null
    private var type: String? = null
    private var serviceType: String? = null
    private var paymentStatus: String? = null
    private var eddSelected = 3
    private var tagSelected = 0

    private val shipmentsMap: MutableMap<String, List<UnassignedDTO>> = mutableMapOf()
    //private val filteredMap: MutableMap<String, List<UnassignedDTO>> = mutableMapOf()

    private var pickupOnFly = false

    private var excludedTrips: MutableList<String> = mutableListOf()
    private var mAdapter: AddShipmentAdapter? = null

    private lateinit var binding: ActivityAddShipmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddShipmentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tripId = intent.getLongExtra(trip_id, 0L)
        pickupOnFly = intent.getBooleanExtra(pickup_onFly, false)

        //PreferenceHelper.init(this)

        initDagger()
        initViews()
    }

    private fun initDagger() {
        DaggerActivityComponents.builder()
            .networkComponent((application as SortationApplication).getNetworkComponent())
            .build().inject(this@AddShipmentActivity)

        mPresenter.onAttach(this, this@AddShipmentActivity)
    }

    private fun initViews() {
        binding.toolBar.toolbarTitle.text = getString(R.string.add_shipments)

        binding.llSearchFilter.tvFilter.setOnClickListener(this)
        binding.toolBar.iVProfileHome.setOnClickListener(this)
        binding.btnPaymentLayout.btnPayment.setOnClickListener(this)

        binding.tvChange.setOnClickListener(this)

        binding.rvPincodeShipments.layoutManager = LinearLayoutManager(this).apply { isSmoothScrollbarEnabled = true}

        binding.btnPaymentLayout.btnPayment.text =
            String.format(getString(R.string.add_shipments_with_count), selectedShipments.size)

        enableOrDisableAddButton()

        binding.llSearchFilter.edtSearchPincode.setOnEditorActionListener { v, actionId, event ->
            if(actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyboard()
                true
            }
            false
        }

        binding.llSearchFilter.edtSearchPincode.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s?.isNotEmpty() == true) {
                    filterByPincode(s.toString())
                    binding.llSearchFilter.edtSearchPincode.setCompoundDrawablesWithIntrinsicBounds(
                        ContextCompat.getDrawable(
                            this@AddShipmentActivity,
                            R.drawable.ic_search
                        ),
                        null,
                        ContextCompat.getDrawable(
                            this@AddShipmentActivity,
                            R.drawable.ic_clear_white
                        ),
                        null
                    )
                } else {
                    binding.llSearchFilter.edtSearchPincode.setCompoundDrawablesWithIntrinsicBounds(
                        ContextCompat.getDrawable(
                            this@AddShipmentActivity,
                            R.drawable.ic_search
                        ), null, null, null
                    )
                    clearFilter()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })

        binding.llSearchFilter.edtSearchPincode.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                if (binding.llSearchFilter.edtSearchPincode.compoundDrawables[2] != null)
                    if (event.rawX >= (binding.llSearchFilter.edtSearchPincode.right - binding.llSearchFilter.edtSearchPincode.compoundDrawables[2].bounds.width())) {
                        binding.llSearchFilter.edtSearchPincode.text.clear()
                        true
                    }
            }
            false
        }

        showProgress()
        mPresenter.getShipments(if (pickupOnFly) ShipmentType.RETURN.name else null, null, null,tag,excludedTrips,serviceType = serviceType,paymentStatus = paymentStatus)
    }

    override fun displayProgress() {
        showProgress()
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.iVProfileHome -> finish()
            R.id.tvFilter -> startFilterActivity()
            R.id.btn_payment -> addShipmentsToTrip()
            R.id.tvChange -> startInTransitActivity()
        }
    }

    private fun startInTransitActivity() {
        val trips = mPresenter.getInTransitTrips()
        val intent = Intent(this, InTransitTripsActivity::class.java)
        intent.putExtra("trips", trips)
        startActivityForResult(intent, INTRANSIT_TRIP)
    }

    override fun onFailure(error: Throwable?) {
        hideProgress()
        processError(error)
    }

    override fun onShipmentsFetched(shipments: Map<String, List<UnassignedDTO>>) {
        shipmentsMap.clear()
        shipmentsMap.putAll(shipments)
        mAdapter = AddShipmentAdapter(this, shipmentsMap, this)
        binding.rvPincodeShipments.adapter = mAdapter
        hideProgress()
    }

    override fun onShipmentSelected(shipments: List<UnassignedDTO>) {
        selectedShipments.addAll(shipments)
        binding.btnPaymentLayout.btnPayment.text =
            String.format(getString(R.string.add_shipments_with_count), selectedShipments.size)
        enableOrDisableAddButton()
    }

    override fun onShipmentUnselected(shipments: List<UnassignedDTO>) {
        selectedShipments.removeAll(shipments)
        binding.btnPaymentLayout.btnPayment.text =
            String.format(getString(R.string.add_shipments_with_count), selectedShipments.size)
        enableOrDisableAddButton()
    }

    override fun onViewAddress(position: Int, data: UnassignedDTO) {
        showProgress()
        mPresenter.getAddressDetails(position, data.shipmentId)
    }

    override fun onMpsCountClicked(shipmentId: String?,id: String?, count: Int) {
        val intent = Intent(this,MPSBoxesActivity::class.java)
        intent.putExtra(shipment_id, shipmentId)
        intent.putExtra("referenceId", id)
        intent.putExtra("boxCount",count)
        startActivity(intent)
    }

    private fun addShipmentsToTrip() {

        val commonRequestList = mutableListOf<CommonRequest>()
        for (shipment in selectedShipments)
            commonRequestList.add(CommonRequest(tripId!!, shipment.referenceId))
        showProgress()
        mPresenter.addShipments(commonRequestList)

    }

    override fun onAddSuccess() {
        hideProgress()
        finish()
    }

    private fun filterByPincode(pin: String) {
        shipmentsMap.clear()
        shipmentsMap.putAll(shipmentsMap.filter { it.key.contains(pin, true) })
        binding.rvPincodeShipments.adapter?.notifyDataSetChanged()
    }

    private fun clearFilter() {
        showProgress()
        mPresenter.getShipments(if (pickupOnFly) ShipmentType.RETURN.name else null, null, null,tag,excludedTrips,serviceType,paymentStatus)
    }

    private fun startFilterActivity() {
        selectedShipments.clear()
        binding.btnPaymentLayout.btnPayment.text =
            String.format(getString(R.string.add_shipments_with_count), selectedShipments.size)
        enableOrDisableAddButton()
        val intent = Intent(this, FilterActivity::class.java)
        intent.putExtra(TASKTYPE, type)
        intent.putExtra(EDD, eddSelected)
        intent.putExtra(TAGS,tagSelected)
        intent.putExtra(pickup_onFly, pickupOnFly)
        intent.putExtra(SERVICETYPE,serviceType)
        intent.putExtra(PAYMENT_STATUS,paymentStatus)

        startActivityForResult(intent, SUCCESS_CODE)
    }

    private fun enableOrDisableAddButton() {
        if (selectedShipments.isNotEmpty()) {
            binding.btnPaymentLayout.btnPayment.isEnabled = true
            binding.btnPaymentLayout.btnPayment.setBackgroundResource(R.drawable.blue_button_background)
        } else {
            binding.btnPaymentLayout.btnPayment.isEnabled = false
            binding.btnPaymentLayout.btnPayment.setBackgroundResource(R.drawable.grey_button_background)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        showProgress()

        if( requestCode == SUCCESS_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                type = data?.getStringExtra("type")
                getEDD = data?.getStringExtra("getEDD")
                tag = data?.getStringExtra("tag")
                eddSelected = data?.getIntExtra("selectedEDD", -1) ?: -1
                tagSelected = data?.getIntExtra("selectedTag", -1) ?: -1
                serviceType = data?.getStringExtra(SERVICETYPE)
                paymentStatus = data?.getStringExtra(PAYMENT_STATUS)

                if (eddSelected == 3)
                    mPresenter.getShipments(
                        if (pickupOnFly) ShipmentType.RETURN.name else type,
                        null,
                        null,
                        tag,
                        excludedTrips,
                        serviceType = serviceType,
                        paymentStatus = paymentStatus
                    )
                else
                    mPresenter.getShipments(
                        if (pickupOnFly) ShipmentType.RETURN.name else type,
                        getTimestampStringForMonth(2),
                        getEDD,
                        tag,
                        excludedTrips,
                        serviceType = serviceType,
                        paymentStatus = paymentStatus
                    )
            } else
                mPresenter.getShipments(
                    if (pickupOnFly) ShipmentType.RETURN.name else null,
                    null,
                    null,
                    null,
                    excludedTrips,
                    serviceType = serviceType,
                    paymentStatus = paymentStatus
                )
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

            mPresenter.getShipments(type, if(eddSelected ==3) getTimestampStringForMonth(2) else null,getEDD, tag, excludedTrips = excludedTrips,serviceType = serviceType,paymentStatus = paymentStatus)
        }
    }

    override fun showIntransitCount(count: Int) {
        hideProgress()
        if (count > 0) {
            binding.clInTransit.visibility = View.VISIBLE
            binding.tvIncludes.text = String.format(getString(R.string.includes_in_transit), count)
        } else {
            binding.clInTransit.visibility = View.GONE
        }
    }

    override fun updateList(position: Int, detail: AddressDTO) {

        shipmentsMap[detail.pincode]!![position].address1 = detail.address1
        shipmentsMap[detail.pincode]!![position].name = detail.name
        shipmentsMap[detail.pincode]!![position].address2 = detail.address2
        shipmentsMap[detail.pincode]!![position].address3 = detail.address3
        shipmentsMap[detail.pincode]!![position].city = detail.city
        shipmentsMap[detail.pincode]!![position].pincode = detail.pincode
        shipmentsMap[detail.pincode]!![position].state = detail.state


        mAdapter?.updateAdapter(detail.pincode!!,position)

        hideProgress()

    }

    override fun onDestroy() {
        super.onDestroy()
        mPresenter.onDetach()
    }
}
