package com.goflash.dispatch.features.dispatch.ui.activity

import android.os.Bundle
import androidx.appcompat.app.ActionBar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.appcompat.widget.Toolbar
import android.view.MenuItem
import android.view.View
import com.goflash.dispatch.R
import com.goflash.dispatch.SortationApplication
import com.goflash.dispatch.databinding.ActivityTripListingBinding
import com.goflash.dispatch.di.components.DaggerActivityComponents
import com.goflash.dispatch.features.dispatch.presenter.TripListPresenter
import com.goflash.dispatch.features.dispatch.view.TripListView
import com.goflash.dispatch.ui.activity.BaseActivity
import com.goflash.dispatch.features.dispatch.ui.adapter.TripAdapter
import com.goflash.dispatch.ui.itemDecoration.MarginItemDecoration
import javax.inject.Inject

class TripListActivity : BaseActivity(), TripListView {

    @Inject
    lateinit var tripListPresenter: TripListPresenter

    private lateinit var binding: ActivityTripListingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTripListingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initDagger()
        initViews()
    }

    private fun initDagger() {
        DaggerActivityComponents.builder().networkComponent((application as SortationApplication).getNetworkComponent())
                .build().inject(this@TripListActivity)

        tripListPresenter.onAttachView(this, this@TripListActivity)
    }

    private fun initViews() {
        val toolbar = findViewById<View>(R.id.toolBar1) as Toolbar
        setSupportActionBar(toolbar)

        binding.toolBar1.toolbarTitle.text = getString(R.string.trip_listing)

        val actionbar: ActionBar? = supportActionBar
        actionbar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_back_button_circular)
            setDisplayShowTitleEnabled(false)
        }

        binding.rvTrip.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        binding.rvTrip.adapter = TripAdapter(this, tripListPresenter)
        binding.rvTrip.addItemDecoration(MarginItemDecoration(resources.getDimension(R.dimen.margin_10).toInt()))

        showProgress()
        tripListPresenter.getTripList()
    }


    override fun refreshList() {
        hideProgress()
        binding.rvTrip.adapter?.notifyDataSetChanged()

    }

    override fun onDestroy() {
        super.onDestroy()
        tripListPresenter.onDetachView()
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
}