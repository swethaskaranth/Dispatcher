package com.goflash.dispatch.ui.activity

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.Toolbar
import com.goflash.dispatch.R
import com.goflash.dispatch.SortationApplication
import com.goflash.dispatch.databinding.ActivityOrderListBinding
import com.goflash.dispatch.di.components.DaggerActivityComponents
import com.goflash.dispatch.features.dispatch.presenter.OrderListPresenter
import com.goflash.dispatch.features.dispatch.view.OrderListView
import com.goflash.dispatch.ui.adapter.OrderAdapter
import com.goflash.dispatch.util.PreferenceHelper
import javax.inject.Inject

class OrdersListActivity : BaseActivity(), OrderListView {

    @Inject
    lateinit var presenter : OrderListPresenter

    private lateinit var binding: ActivityOrderListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initDagger()
        initViews()
    }


    private fun initDagger(){
        DaggerActivityComponents.builder().networkComponent((application as SortationApplication).getNetworkComponent())
                .build().inject(this@OrdersListActivity)

        presenter.onAttachView(this@OrdersListActivity,this)
    }


    private fun initViews(){
        val toolbar = findViewById<View>(R.id.toolBar1) as Toolbar
        setSupportActionBar(toolbar)

        binding.toolBar1.toolbarTitle.text = getString(R.string.list_of_orders)

        val actionbar: ActionBar? = supportActionBar
        actionbar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp)
            setDisplayShowTitleEnabled(false)
        }

        binding.orderDetail.itemLabel.text = getString(R.string.bin_no)

        binding.chemistName.text = PreferenceHelper.assignedAssetName

        binding.cancelledList.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        binding.cancelledList.adapter = OrderAdapter(this@OrdersListActivity,presenter)
    }

    override fun showOrderCount(binNumber : String?) {
        binding.orderDetail.orderId.text = binNumber
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            android.R.id.home -> {

                finish()
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onFailure(message: String) {
        hideProgress()
        errorMessage(message)

    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDetachView()
    }



}