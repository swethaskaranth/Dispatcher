package com.goflash.dispatch.features.receiving.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import co.uk.rushorm.core.RushCore
import co.uk.rushorm.core.RushSearch
import com.goflash.dispatch.R
import com.goflash.dispatch.SortationApplication
import com.goflash.dispatch.app_constants.TRIPID
import com.goflash.dispatch.app_constants.VEHICLEID
import com.goflash.dispatch.app_constants.show_completed
import com.goflash.dispatch.data.ReceivingDto
import com.goflash.dispatch.data.VehicleDetails
import com.goflash.dispatch.databinding.ActivityReceivingBinding
import com.goflash.dispatch.di.components.DaggerActivityComponents
import com.goflash.dispatch.features.receiving.adapter.ReceivingAdapter
import com.goflash.dispatch.features.receiving.presenter.ReceivingPresenter
import com.goflash.dispatch.features.receiving.view.ReceivingView
import com.goflash.dispatch.listeners.ItemSelectedListener
import com.goflash.dispatch.type.PackageStatus
import com.goflash.dispatch.ui.activity.BaseActivity
import javax.inject.Inject

class ReceivingActivity : BaseActivity(), ReceivingView, ItemSelectedListener {

    @Inject
    lateinit var mPresenter: ReceivingPresenter

    private var items = mutableListOf<ReceivingDto>()
    private lateinit var adapter: ReceivingAdapter

    private var showCompleted = true

    private lateinit var binding: ActivityReceivingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReceivingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        showCompleted = intent.getBooleanExtra(show_completed, true)

        initDagger()
        initViews()
    }

    private fun initDagger() {

        DaggerActivityComponents.builder().networkComponent((application as SortationApplication).getNetworkComponent())
            .build().inject(this@ReceivingActivity)

        mPresenter.onAttachView(this, this)
    }

    private fun initViews() {

        binding.toolBar1.toolbarTitle.text = getString(R.string.expected)

        val toolbar = findViewById<View>(R.id.toolBar1) as Toolbar
        setSupportActionBar(toolbar)

        val actionbar: ActionBar? = supportActionBar
        actionbar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_back_button_circular)
            setDisplayShowTitleEnabled(false)
        }

        binding.layoutReceiving.rvReceivingList.layoutManager = LinearLayoutManager(this)
        adapter = ReceivingAdapter(this@ReceivingActivity, items, this)
        binding.layoutReceiving.rvReceivingList.adapter = adapter

        if (showCompleted)
            binding.toolBar1.toolbarTitle.text = getString(R.string.received_consignments)

        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(str: Editable?) {
                if(str?.length!! > 0)
                    mPresenter.searchByVehicleSeal(str.toString())
                else
                    mPresenter.clearFilter()
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

        }

        binding.layoutReceiving.etSearch.addTextChangedListener(textWatcher)

    }

    override fun onResume() {
        super.onResume()
        mPresenter.getAllTasks()
    }

    override fun onFailure(error: Throwable?) {
        hideProgress()
        if (error != null)
            processError(error)
    }

    override fun onSuccess(receivingTasks: MutableList<ReceivingDto>) {
        hideProgress()

        items.clear()

        val receivingDTO = RushSearch().find(ReceivingDto::class.java)
        receivingDTO?.let {
            RushCore.getInstance().delete(it)
        }


        receivingTasks.forEach {
            it.save()
        }

        if (!showCompleted)
            receivingTasks.removeAll { it.status == PackageStatus.RECON_FINISHED.name }
        else {
            receivingTasks.removeAll { it.status != PackageStatus.RECON_FINISHED.name }
        }

        // receivingTasks.sortByDescending { it.status != PackageStatus.RECON_FINISHED.name }


        items.addAll(receivingTasks)
        adapter.notifyDataSetChanged()
    }

    override fun onItemSelected(item: ReceivingDto) {
        if (item.status == PackageStatus.OUT_FOR_DELIVERY.name) {
            val bagDetails = RushSearch().whereEqual(VEHICLEID, item.vehicleId).findSingle(
                VehicleDetails::class.java)

            if (bagDetails != null || item.vehicleId == null ) {
                val intent = Intent(this, ReceiveBagActivity::class.java)
                intent.putExtra(VEHICLEID, item.vehicleId)
                intent.putExtra(TRIPID, item.tripId.toString())
                startActivity(intent)
            } else {
                val intent = Intent(this, VehicleScanActivity::class.java)
                intent.putExtra(VEHICLEID, item.vehicleId)
                intent.putExtra(TRIPID, item.tripId.toString())
                startActivity(intent)
            }
        } else if (item.status == PackageStatus.RECON_FINISHED.name) {
            val intent = Intent(this, ConsignmentDetailActivity::class.java)
            intent.putExtra(TRIPID, item.tripId)
            startActivity(intent)
        }
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



    override fun onDestroy() {
        super.onDestroy()
        mPresenter.onDetachView()
    }
}
