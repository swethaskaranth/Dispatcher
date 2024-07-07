package com.goflash.dispatch.features.rtoReceiving.ui.activity

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.goflash.dispatch.R
import com.goflash.dispatch.data.InwardRunItem
import com.goflash.dispatch.databinding.LayoutMpsInwardrunitemsBinding
import com.goflash.dispatch.features.rtoReceiving.ui.adapter.MpsRunitemAdapter
import com.goflash.dispatch.features.rtoReceiving.view.MpsInwardRunItemsView
import com.goflash.dispatch.ui.activity.BaseActivity
import com.goflash.dispatch.ui.itemDecoration.VerticalMarginItemDecoration

class MpsInwardRunItemsActivity: BaseActivity(), MpsInwardRunItemsView {

    lateinit var binding: LayoutMpsInwardrunitemsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = LayoutMpsInwardrunitemsBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val totalCount = intent.getIntExtra("total",0)
        binding.tvNoOfBoxes.text = String.format(getString(R.string._no_boxes_in_this_shipment), totalCount)

        val items = intent.getParcelableArrayExtra("mpsRunItems")?.map { it as InwardRunItem }?.toMutableList()

        binding.rvShipments.layoutManager = LinearLayoutManager(this)
        binding.rvShipments.adapter = MpsRunitemAdapter(this, items!!)
        binding.rvShipments.addItemDecoration(VerticalMarginItemDecoration( resources.getDimension(
            R.dimen.margin_12
        ).toInt()))

        binding.ivClose.setOnClickListener { finish() }

    }
}