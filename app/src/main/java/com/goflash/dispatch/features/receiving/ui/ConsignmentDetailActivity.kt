package com.goflash.dispatch.features.receiving.ui

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import co.uk.rushorm.core.RushSearch
import com.goflash.dispatch.R
import com.goflash.dispatch.SortationApplication
import com.goflash.dispatch.app_constants.TRIPID
import com.goflash.dispatch.data.ReceivingDto
import com.goflash.dispatch.data.ShipmentCount
import com.goflash.dispatch.databinding.ActivityConsignmentDetailBinding
import com.goflash.dispatch.di.components.DaggerActivityComponents
import com.goflash.dispatch.features.receiving.adapter.ConsignmentDetailAdapter
import com.goflash.dispatch.features.receiving.presenter.ConsignmentDetailPresenter
import com.goflash.dispatch.features.receiving.view.ConsignmentDetailView
import com.goflash.dispatch.ui.activity.BaseActivity
import javax.inject.Inject

class ConsignmentDetailActivity : BaseActivity() , ConsignmentDetailView{

    @Inject
    lateinit var presenter : ConsignmentDetailPresenter

    private var tripId : Long= 0

    private lateinit var binding: ActivityConsignmentDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConsignmentDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initDagger()
        initViews()
    }


    private fun initViews(){
        binding.toolBar1.toolbarTitle.text = getString(R.string.consignment_detail)

        val toolbar = findViewById<View>(R.id.toolBar1) as Toolbar
        setSupportActionBar(toolbar)

        val actionbar: ActionBar? = supportActionBar
        actionbar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_back_button_circular)
            setDisplayShowTitleEnabled(false)
        }
    }


    private fun initDagger(){
        DaggerActivityComponents.builder().networkComponent((application as SortationApplication).getNetworkComponent())
                .build().inject(this@ConsignmentDetailActivity)

        presenter.onAttachView(this,this)

        showProgress()
        tripId = intent?.getLongExtra(TRIPID,0)?:0
        presenter.getBagShipments(tripId)

    }

    override fun onSuccess(list: ArrayList<ShipmentCount>) {
        hideProgress()
        binding.rvBagCount.layoutManager = LinearLayoutManager(this)
        binding.rvBagCount.adapter = ConsignmentDetailAdapter(this,list)

        val shipmentCount = list.sumBy { it.shipmentCount }

        val item = RushSearch().whereEqual("tripId",tripId).findSingle(ReceivingDto::class.java)


        binding.orderDetail.itemLabel.text = "${item?.vehicleId?:tripId}   |   ${item.assetName}   |   ${item.agentName}"
        binding.label.text = String.format(getString(R.string.received_bags_shipments),list.size, shipmentCount)

    }

    override fun onFailure(error: Throwable?) {
        hideProgress()
        processError(error)
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
        presenter.onDetachView()
    }
}