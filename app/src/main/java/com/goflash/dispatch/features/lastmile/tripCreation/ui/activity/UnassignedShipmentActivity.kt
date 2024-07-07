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
import com.goflash.dispatch.databinding.LayoutUnassignedShipmentsBinding
import com.goflash.dispatch.di.components.DaggerActivityComponents
import com.goflash.dispatch.features.lastmile.tripCreation.listeners.FragmentListener
import com.goflash.dispatch.features.lastmile.tripCreation.listeners.UnassignedShipmentListener
import com.goflash.dispatch.features.lastmile.tripCreation.presenter.UnassignedShipmentPresenter
import com.goflash.dispatch.features.lastmile.tripCreation.ui.adapter.UnassignedAdapter
import com.goflash.dispatch.features.lastmile.tripCreation.ui.fragments.BottomSheetCancelFragment
import com.goflash.dispatch.features.lastmile.tripCreation.ui.fragments.BottomSheetUnblockFragment
import com.goflash.dispatch.features.lastmile.tripCreation.view.UnassignedView
import com.goflash.dispatch.model.AddressDTO
import com.goflash.dispatch.model.ShipmentDTO
import com.goflash.dispatch.type.PriorityType
import com.goflash.dispatch.ui.activity.BaseActivity
import com.goflash.dispatch.util.getPostponedDateStringFrom
import com.goflash.dispatch.util.getTimestampStringForMonth
import rx.subjects.PublishSubject
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class UnassignedShipmentActivity : BaseActivity(), View.OnClickListener, UnassignedView,
    UnassignedShipmentListener, FragmentListener {


    @Inject
    lateinit var presenter: UnassignedShipmentPresenter

    private var mList = mutableListOf<UnassignedDTO>()

    private var getEDD: String? = null
    private var tag: String? = null
    private var type: String? = null
    private var serviceType: String? = null
    private var paymentStatus: String? = null
    private var eddSelected = 3
    private var tagSelected = 0

    private var eddStart: String? = null
    private var eddEnd: String? = null

    private lateinit var subject: PublishSubject<String>

    private var excludedTrips: MutableList<String> = mutableListOf()

    private lateinit var binding: LayoutUnassignedShipmentsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LayoutUnassignedShipmentsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initDagger()
        initViews()
    }

    private fun initViews() {
        binding.toolBar.toolbarTitle.text = getString(R.string.unassigned_shipments)
        binding.llSearchFilter.tvFilter.setOnClickListener(this)
        binding.toolBar.iVProfileHome.setOnClickListener(this)
        binding.llSearchFilter.tvSearchById.setOnClickListener(this)
        binding.llSearchFilter.ivScanSearch.setOnClickListener(this)
        binding.tvChange.setOnClickListener(this)

        binding.rvUnassignedShipments.layoutManager = LinearLayoutManager(this)
        binding.rvUnassignedShipments.adapter = UnassignedAdapter(this, mList, this)

        binding.shimmerViewContainer.startShimmer()
        binding.shimmerViewContainer.visibility = View.VISIBLE

        presenter.getShipments(null, null, null, null, null, excludedTrips,serviceType,paymentStatus)

        subject = PublishSubject.create()
        subject.debounce(FILTER_DEBOUNCE, TimeUnit.MILLISECONDS)
            .subscribe {
                runOnUiThread {
                    filter(it)
                }
            }

        binding.llSearchFilter.tvSearchById.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable?) {
                if (s?.isNotEmpty() == true) {
                    subject.onNext(s.toString().trim().toLowerCase())
                    binding.llSearchFilter.tvSearchById.setCompoundDrawablesWithIntrinsicBounds(
                        ContextCompat.getDrawable(
                            this@UnassignedShipmentActivity,
                            R.drawable.ic_search
                        ),
                        null,
                        ContextCompat.getDrawable(
                            this@UnassignedShipmentActivity,
                            R.drawable.ic_clear_white
                        ),
                        null
                    )
                } else {
                    binding.llSearchFilter.tvSearchById.setCompoundDrawablesWithIntrinsicBounds(
                        ContextCompat.getDrawable(
                            this@UnassignedShipmentActivity,
                            R.drawable.ic_search
                        ), null, null, null
                    )
                    clearFilter()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })

        binding.llSearchFilter.tvSearchById.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                if (binding.llSearchFilter.tvSearchById.compoundDrawables[2] != null)
                    if (event.rawX >= (binding.llSearchFilter.tvSearchById.right - binding.llSearchFilter.tvSearchById.compoundDrawables[2].bounds.width())) {
                        binding.llSearchFilter.tvSearchById.text.clear()
                        true
                    }
            }
            false
        }

        binding.llSearchFilter.tvSearchById.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard()
            }
            true
        }

    }

    private fun initDagger() {
        DaggerActivityComponents.builder()
            .networkComponent((application as SortationApplication).getNetworkComponent()).build()
            .inject(this@UnassignedShipmentActivity)

        presenter.onAttach(this, this)
    }

    private fun filter(query: String) {
        if (query.length > 3)
            presenter.getShipments(null, null, null, query, null, excludedTrips,serviceType,paymentStatus)
    }

    private fun clearFilter() {
        presenter.getShipments(null, null, null, null, null, excludedTrips,serviceType,paymentStatus)
    }

    override fun displayProgress() {
        showProgress()
    }

    override fun onShipmentsFetched(list: List<UnassignedDTO>) {

        mList.clear()
        mList.addAll(list)
        val nullPriority = mList.filter { it.priorityType == null }
        mList.removeAll(nullPriority)
        mList.sortedByDescending { it.priorityType == PriorityType.HIGH.name }
        mList.addAll(nullPriority)
        binding.rvUnassignedShipments.adapter?.notifyDataSetChanged()

        binding.shimmerViewContainer.stopShimmer()
        binding.shimmerViewContainer.visibility = View.GONE
        binding.rvUnassignedShipments.visibility = View.VISIBLE

    }

    override fun onFailure(error: Throwable?) {
        hideProgress()
        processError(error)
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
        hideProgress()

        mList[position].name = detail.name
        mList[position].address1 = detail.address1
        mList[position].address2 = detail.address2
        mList[position].address3 = detail.address3
        mList[position].city = detail.city
        mList[position].state = detail.state
        mList[position].pincode = detail.pincode

        binding.rvUnassignedShipments.adapter?.notifyDataSetChanged()
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.iVProfileHome -> finish()
            R.id.tvFilter -> startFilterActivity()
            R.id.ivScanSearch -> startScanActivity()
            R.id.tvChange -> startInTransitActivity()

        }
    }

    private fun startInTransitActivity() {
        val trips = presenter.getInTransitTrips()
        val intent = Intent(this, InTransitTripsActivity::class.java)
        intent.putExtra("trips", trips)
        startActivityForResult(intent, INTRANSIT_TRIP)
    }

    private fun startScanActivity() {
        startActivity(Intent(this, ScanToSearchActivity::class.java))
    }

    private fun startFilterActivity() {
        val intent = Intent(this, FilterActivity::class.java)
        intent.putExtra(TASKTYPE, type)
        intent.putExtra(EDD, eddSelected)
        intent.putExtra(TAGS, tagSelected)
        intent.putExtra(SERVICETYPE,serviceType)
        intent.putExtra(PAYMENT_STATUS,paymentStatus)
        startActivityForResult(intent, SUCCESS_CODE)
    }

    override fun onShipmentSelected(position: Int) {
    }

    override fun onDeleteShipemnt(position: Int, data: UnassignedDTO) {

        val id = data.packageId ?: data.referenceId
        val intent = Intent(this, SelectCancelReason::class.java)
        intent.putExtra("referenceId", id)
        intent.putExtra("shipmentId", data.shipmentId)
        intent.putExtra("position", position)
        intent.putExtra("type",data.shipmentType)
        startActivityForResult(intent, SUCCESS_CANCEL)
    }

    override fun onEditShipment(position: Int, data: UnassignedDTO) {
        val frag = BottomSheetUnblockFragment()
        val args = Bundle()
        val id = data.packageId ?: data.referenceId
        args.putString("referenceId", id)
        args.putString("shipmentId", data.shipmentId)
        args.putInt("position", position)
        args.putString("postponed", getPostponedDateStringFrom(data.postponedToDate!!))
        frag.arguments = args
        frag.isCancelable = false
        frag.show(supportFragmentManager, frag.tag)
    }

    override fun onViewDetails(position: Int, data: UnassignedDTO) {
        showProgress()
        presenter.getAddressDetails(position, data.shipmentId)
    }

    override fun onMpsCountClicked(position: Int) {
        val intent = Intent(this,MPSBoxesActivity::class.java)
        intent.putExtra(shipment_id, mList[position].shipmentId)
        val id = mList[position].packageId ?: mList[position].referenceId
        intent.putExtra("referenceId", id)
        intent.putExtra("boxCount",mList[position].mpsCount)
        startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        binding.shimmerViewContainer.startShimmer()
        binding.shimmerViewContainer.visibility = View.VISIBLE
        binding.rvUnassignedShipments.visibility = View.GONE

        if (requestCode == SUCCESS_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                type = data?.getStringExtra("type")
                serviceType = data?.getStringExtra(SERVICETYPE)
                paymentStatus = data?.getStringExtra(PAYMENT_STATUS)
                getEDD = data?.getStringExtra("getEDD")
                tag = data?.getStringExtra("tag")
                eddSelected = data?.getIntExtra("selectedEDD", -1) ?: -1
                tagSelected = data?.getIntExtra("selectedTag", -1) ?: -1
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
                presenter.getShipments(type, eddStart, eddEnd, null, tag, excludedTrips,serviceType,paymentStatus)
            } else
                presenter.getShipments(null, null, null, null, tag, excludedTrips,serviceType,paymentStatus)
        } else if (requestCode == INTRANSIT_TRIP) {
            if (resultCode == Activity.RESULT_OK) {
                val trips = data?.getSerializableExtra("tripsSelected") as ArrayList<InTransitTrip>
                presenter.setInTransitTrips(trips)
                excludedTrips.clear()
                for(trip in trips)
                    if(!trip.selected)
                        excludedTrips.add(trip.id.toString())
                binding.tvIncludes.text = String.format(getString(R.string.includes_in_transit), trips.filter { it.selected }.size)
            }

            presenter.getShipments(type, eddStart, eddEnd, null, tag, excludedTrips,serviceType,paymentStatus)

        }

    }

    override fun callDeliveryAgent(phoneNumber: String) {

    }

    override fun commonListener() {

    }

    override fun deleteOrUnblockShipment(position: Int) {
        binding.shimmerViewContainer.startShimmer()
        binding.shimmerViewContainer.visibility = View.VISIBLE
        binding.rvUnassignedShipments.visibility = View.GONE
        presenter.getShipments(null, null, null, null, null, excludedTrips,serviceType,paymentStatus)
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDetach()
    }
}