package com.goflash.dispatch.features.lastmile.tripCreation.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.goflash.dispatch.R
import com.goflash.dispatch.data.InTransitTrip
import com.goflash.dispatch.databinding.LayoutInTransitTripsBinding
import com.goflash.dispatch.features.lastmile.tripCreation.listeners.InTransitTripListener
import com.goflash.dispatch.features.lastmile.tripCreation.ui.adapter.InTransitTripAdapter
import com.goflash.dispatch.ui.activity.BaseActivity

class InTransitTripsActivity : BaseActivity(), InTransitTripListener,
    View.OnClickListener {

    private var trips: ArrayList<InTransitTrip> = ArrayList()

    private lateinit var binding: LayoutInTransitTripsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LayoutInTransitTripsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        trips = intent?.extras?.getSerializable("trips") as ArrayList<InTransitTrip>

        initViews()
    }

    private fun initViews() {

        binding.rvInTransitTrips.layoutManager = LinearLayoutManager(this)
        binding.rvInTransitTrips.adapter = InTransitTripAdapter(this, trips, this)

        binding.bottomLayout.btnClose.setOnClickListener(this)
        binding.bottomLayout.btnConfirm.setOnClickListener(this)
    }

    override fun onSelectOrDeselectAll(select: Boolean) {
        trips.map { it.selected = select }
        binding.rvInTransitTrips.adapter?.notifyDataSetChanged()
    }

    override fun onSelectOrDeselectItem(select: Boolean, position: Int) {
        trips[position].selected = select
        binding.rvInTransitTrips.adapter?.notifyDataSetChanged()
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.btnClose -> {
                setResult(Activity.RESULT_CANCELED, intent)
                finish()
            }
            R.id.btnConfirm -> {
                val intent = Intent()
                intent.putExtra("tripsSelected",trips)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        }
    }

}