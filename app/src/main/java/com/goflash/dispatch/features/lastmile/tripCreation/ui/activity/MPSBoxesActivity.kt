package com.goflash.dispatch.features.lastmile.tripCreation.ui.activity

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.goflash.dispatch.R
import com.goflash.dispatch.SortationApplication
import com.goflash.dispatch.app_constants.shipment_id
import com.goflash.dispatch.data.ChildShipmentDTO
import com.goflash.dispatch.databinding.LayoutMpsShipmentsBinding
import com.goflash.dispatch.di.components.DaggerActivityComponents
import com.goflash.dispatch.features.lastmile.tripCreation.presenter.MPSBoxesPresenter
import com.goflash.dispatch.features.lastmile.tripCreation.ui.adapter.MPSBoxAdapater
import com.goflash.dispatch.features.lastmile.tripCreation.view.MPSBoxesView
import com.goflash.dispatch.ui.activity.BaseActivity
import javax.inject.Inject

class MPSBoxesActivity : BaseActivity(), MPSBoxesView {

    @Inject
    lateinit var mPresenter: MPSBoxesPresenter

    private var parentShipmentId: String? = null
    private var boxCount: Int = 0

    private var referenceId: String? = null

    private var childShipments: List<ChildShipmentDTO>? = null
    private lateinit var binding: LayoutMpsShipmentsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LayoutMpsShipmentsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        parentShipmentId = intent.getStringExtra(shipment_id)
        childShipments = intent.getParcelableArrayListExtra("childShipments")
        boxCount = if (childShipments == null) {
            intent.getIntExtra("boxCount", 0)
        } else
            childShipments!!.size

        referenceId = intent.getStringExtra("referenceId")

        initDagger()
        initViews()

    }

    private fun initDagger() {
        DaggerActivityComponents.builder()
            .networkComponent((application as SortationApplication).getNetworkComponent())
            .build().inject(this@MPSBoxesActivity)

        mPresenter.onAttach(this, this@MPSBoxesActivity)
        if (childShipments == null)
            mPresenter.fetchChildShipments(referenceId!!)
        else
            onChildShipmentsFetched(childShipments!!)

    }

    private fun initViews() {
        binding.tvNoOfBoxes.text = String.format(getString(R.string._no_boxes_in_this_shipment), boxCount)

        binding.ivClose.setOnClickListener { finish() }
    }

    override fun onChildShipmentsFetched(shipments: List<ChildShipmentDTO>) {
        binding.rvBoxes.layoutManager = LinearLayoutManager(this)
        binding.rvBoxes.adapter = MPSBoxAdapater(this, shipments)
    }

    override fun onFailure(error: Throwable?) {
        processError(error)
    }

    override fun onDestroy() {
        super.onDestroy()
        mPresenter.onDetach()
    }
}