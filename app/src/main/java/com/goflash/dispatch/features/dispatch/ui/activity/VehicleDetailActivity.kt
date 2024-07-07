package com.goflash.dispatch.features.dispatch.ui.activity

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.core.content.ContextCompat
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.Toolbar
import android.util.Log
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.ArrayAdapter
import androidx.core.widget.addTextChangedListener

import com.goflash.dispatch.R
import com.goflash.dispatch.SortationApplication
import com.goflash.dispatch.app_constants.*
import com.goflash.dispatch.data.Sprinter
import com.goflash.dispatch.databinding.LayoutVehicleDetailsBinding
import com.goflash.dispatch.di.components.DaggerActivityComponents
import com.goflash.dispatch.features.dispatch.presenter.VehicleDetailPresenter
import com.goflash.dispatch.features.dispatch.view.VehicleDetailView
import com.goflash.dispatch.ui.activity.BaseActivity
import javax.inject.Inject

class VehicleDetailActivity : BaseActivity(), VehicleDetailView, View.OnClickListener {

    @Inject
    lateinit var vehicleDetailPresenter: VehicleDetailPresenter

    private var spinner_initialized = false

    lateinit var textWatcher: TextWatcher

    var transportMode = "ROAD"
    var disableVehicleNumber = false

    private lateinit var binding: LayoutVehicleDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LayoutVehicleDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initDagger()
        initViews()
    }

    private fun initDagger() {
        DaggerActivityComponents.builder()
            .networkComponent((application as SortationApplication).getNetworkComponent())
            .build().inject(this@VehicleDetailActivity)

        vehicleDetailPresenter.onAttachView(this, this)
        vehicleDetailPresenter.sendIntent(intent)
    }

    private fun initViews() {
        val toolbar = findViewById<View>(R.id.toolBar) as Toolbar
        setSupportActionBar(toolbar)

        val actionbar: ActionBar? = supportActionBar
        actionbar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_back_button_circular)
            setDisplayShowTitleEnabled(false)
        }

        binding.toolBar.toolbarTitle.text = getString(R.string.vehicle_and_driver_details)

        binding.proceedBtn.text = getString(R.string.proceed)
        binding.proceedBtn.isEnabled = false
        binding.proceedBtn.setBackgroundResource(R.drawable.disable_button)

        binding.proceedBtn.setOnClickListener(this)
        binding.btnSave.setOnClickListener(this)

        binding.rgMode.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbRoad -> {
                    transportMode = "ROAD"
                }
                R.id.rbOther -> {
                    transportMode = "RAIL"
                }
            }
            enableOrDisableVehicleNumber(true)
        }

        binding.btnSave.isEnabled = false

        textWatcher = object : TextWatcher {
            override fun afterTextChanged(text: Editable?) {
                enableOrDisableSaveButton(text?.toString()?.isNotEmpty() == true)
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }
        }

        binding.tvVehicleNumber.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                if (binding.tvVehicleNumber.compoundDrawables[2] != null)
                    if (event.rawX >= (binding.tvVehicleNumber.right - binding.tvVehicleNumber.compoundDrawables[2].bounds.width())) {
                        editVehicleNumber(binding.tvVehicleNumber.text.toString())
                        true
                    }
            }
            false
        }

        showProgress()
        vehicleDetailPresenter.getSprinterList()
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.proceed_btn -> {
                binding.proceedBtn.isEnabled = false
                showProgress()
                vehicleDetailPresenter.onProceedClicked(transportMode)
            }
            R.id.btnSave -> {
                hideKeyboard()
                vehicleDetailPresenter.validateVehicleNumber(binding.etVehicleNumber.text.toString())
            }
        }
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
        vehicleDetailPresenter.onDetachView()
    }


    override fun onSprinterListFetched(sprinters: MutableList<String>, btnText: String) {
        hideProgress()

        val spinnerAdapter =
            ArrayAdapter(this, R.layout.item_sprinter_spinner, R.id.sprinter_name, sprinters)
        binding.sprinterSpinner.setAdapter(spinnerAdapter)
        binding.sprinterSpinner.threshold = 0

        binding.sprinterSpinner.setOnItemClickListener { adapterView, view, position, id ->
            showProgress()

            vehicleDetailPresenter.onSprinterSelected(adapterView.getItemAtPosition(position) as String)
        }

        binding.sprinterSpinner.setOnTouchListener { p0, p1 ->
            binding.sprinterSpinner.showDropDown()
            false
        }

        binding.proceedBtn.text = btnText

    }

    override fun enableProceedBtn() {
        hideProgress()
        if (!disableVehicleNumber && transportMode == "ROAD" && binding.tvVehicleNumber.text.isNullOrEmpty()) {
            binding.proceedBtn.isEnabled = false
            binding.proceedBtn.setBackgroundColor(ContextCompat.getColor(this, R.color.border_blue))
        } else {
            binding.proceedBtn.isEnabled = true
            binding.proceedBtn.setBackgroundColor(ContextCompat.getColor(this, R.color.md_orange_800))
        }
    }

    override fun startVehcileSealScanActivity(selectedSprinter: Sprinter) {
        hideProgress()
        val intent = Intent(this, DispatchVehicleActivity::class.java)
        intent.putExtra(sprinter, selectedSprinter)
        intent.putExtra(transMode, transportMode)
        startActivity(intent)
    }

    override fun onFailure(error: Throwable?) {
        hideProgress()
        processError(error)
        binding.proceedBtn.isEnabled = true
    }

    override fun onSuccess(
        message: String,
        tripId: String,
        sprinter: String,
        invoiceRequired: Boolean
    ) {
        hideProgress()
        showToast(this, message)
        val intent = Intent(this, DispatchVehicleActivity::class.java)
        intent.putExtra(seal_required, false)
        intent.putExtra(trip_id, tripId)
        intent.putExtra(sprinter_name, sprinter)
        intent.putExtra(invoice_required, invoiceRequired)
        startActivity(intent)
    }

    override fun showInvalidVehicleNumber() {
        binding.tvError.visibility = View.VISIBLE
        binding.clVehicleNumber.setBackgroundResource(R.drawable.vehicle_number_error_background)
    }

    private fun editVehicleNumber(vehicleNumber: String?) {
        binding.tvVehicleNumber.text = ""
        binding.tvVehicleNumber.visibility = View.GONE
        binding.clVehicleNumber.visibility = View.VISIBLE
        binding.etVehicleNumber.setText(vehicleNumber)
        binding.etVehicleNumber.addTextChangedListener(textWatcher)
        enableOrDisableSaveButton(false)
        enableProceedBtn()
    }

    override fun setVehicleNumber(
        vehicleNumber: String?,
        showSuccess: Boolean,
        showNotMapped: Boolean
    ) {
        binding.tvError.visibility = View.GONE
        binding.clVehicleNumber.setBackgroundResource(R.drawable.vehicle_number_background)
        if (vehicleNumber == null) {
            binding.clNoVehicleMapped.visibility = if (showNotMapped) View.VISIBLE else View.GONE
            binding.clVehicleMapped.visibility = View.GONE
            binding.clVehicleNumber.visibility = View.VISIBLE
            binding.tvVehicleNumber.text = ""
            binding.tvVehicleNumber.visibility = View.GONE
            binding.etVehicleNumber.text.clear()
            binding.etVehicleNumber.addTextChangedListener(textWatcher)
        } else {
            binding.clVehicleMapped.visibility = if (showSuccess) View.VISIBLE else View.GONE
            binding.clNoVehicleMapped.visibility = View.GONE
            binding.clVehicleNumber.visibility = View.GONE
            binding.tvVehicleNumber.visibility = View.VISIBLE
            binding.tvVehicleNumber.text = vehicleNumber
        }
    }

    override fun enableOrDisableVehicleNumber(modeChanged: Boolean) {
        if (transportMode == "ROAD") {
            if (modeChanged)
                vehicleDetailPresenter.getSelectedSprinterDetails()
            binding.tvVehicleNumber.setCompoundDrawablesWithIntrinsicBounds(
                null, null, ContextCompat.getDrawable(
                    this@VehicleDetailActivity,
                    R.drawable.ic_edit
                ), null
            )
        } else {
            binding.clNoVehicleMapped.visibility = View.GONE
            binding.clVehicleMapped.visibility = View.GONE
            binding.clVehicleNumber.visibility = View.GONE
            binding.tvVehicleNumber.visibility = View.VISIBLE
            binding.tvVehicleNumber.text = ""
            binding.tvVehicleNumber.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
        }

    }

    private fun enableOrDisableSaveButton(enable: Boolean) {
        binding.btnSave.also {
            if (enable) {
                it.isEnabled = true
                it.setBackgroundResource(R.drawable.save_button_background_active)
            } else {
                it.isEnabled = false
                it.setBackgroundResource(R.drawable.save_button_background_inactive)
            }
        }
    }

    override fun disableVehicleNumber() {
        disableVehicleNumber = true

        for (i in 0 until binding.rgMode.childCount) {
            binding.rgMode.getChildAt(i).isEnabled = false
        }

        binding.clNoVehicleMapped.visibility = View.GONE
        binding.clVehicleMapped.visibility = View.GONE
        binding.clVehicleNumber.visibility = View.GONE
        binding.tvVehicleNumber.visibility = View.VISIBLE
        binding.tvVehicleNumber.text = ""
        binding.tvVehicleNumber.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
       // rgMode.isEnabled = false
    }
}