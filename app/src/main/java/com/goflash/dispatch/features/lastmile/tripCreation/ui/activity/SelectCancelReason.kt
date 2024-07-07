package com.goflash.dispatch.features.lastmile.tripCreation.ui.activity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.RadioButton
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.goflash.dispatch.R
import com.goflash.dispatch.SortationApplication
import com.goflash.dispatch.databinding.ActivitySelectCancelReasonBinding
import com.goflash.dispatch.di.components.DaggerActivityComponents
import com.goflash.dispatch.features.lastmile.tripCreation.listeners.InTransitTripListener
import com.goflash.dispatch.features.lastmile.tripCreation.presenter.SelectReasonPresenter
import com.goflash.dispatch.features.lastmile.tripCreation.view.ReasonView
import com.goflash.dispatch.model.PincodeVerify
import com.goflash.dispatch.model.ShipmentDTO
import com.goflash.dispatch.type.ShipmentType
import com.goflash.dispatch.ui.activity.BaseActivity
import com.goflash.dispatch.ui.adapter.ChildShipmentAdapter
import com.goflash.dispatch.util.PreferenceHelper
import javax.inject.Inject

class SelectCancelReason : BaseActivity(), ReasonView, View.OnClickListener, TextWatcher,
    InTransitTripListener {

    private val red by lazy {
        ContextCompat.getColor(this, R.color.reject_red)
    }

    private val green by lazy {
        ContextCompat.getColor(this, R.color.accept_green)
    }

    @Inject
    lateinit var mPresenter: SelectReasonPresenter

    private var selectReason: String? = null
    private var shipmentId: String? = null
    private var referenceId: String? = null
    private var assetId: String? = null
    private var assetName: String? = null
    private var serviceable: Boolean? = false
    private var shipmentType: String? = null

    private var childShipments: MutableList<ShipmentDTO> = mutableListOf()
    private var selectedChildShipments: MutableList<ShipmentDTO> = mutableListOf()

    private lateinit var binding: ActivitySelectCancelReasonBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectCancelReasonBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initDagger()
        initViews()
    }

    private fun initViews() {

        shipmentId = intent.getStringExtra("shipmentId")
        referenceId = intent.getStringExtra("referenceId")
        shipmentType = intent.getStringExtra("type")

        if (shipmentType == ShipmentType.MPS.name)
            mPresenter.getChildShipments(referenceId!!)
        binding.layoutCancelReason.tvReferenceId.text = String.format(getString(R.string.reference_id), referenceId)

        if(shipmentType == ShipmentType.RTO.name)
            binding.layoutCancelReason.rbOutOfServiceableArea.visibility = View.GONE

        binding.saveLayout.btnClear.text = getString(R.string.close)
        binding.saveLayout.btnSave.text = getString(R.string.confirm)

        binding.saveLayout.btnClear.setOnClickListener(this)
        binding.saveLayout.btnSave.setOnClickListener(this)
        binding.layoutCancelReason.btnUpdate.setOnClickListener(this)
        binding.layoutCancelReason.edPincode.addTextChangedListener(this)

        binding.saveLayout.btnSave.isEnabled = false
        binding.saveLayout.btnSave.setBackgroundResource(R.drawable.disable_button)

        setRadioButton()
    }

    private fun initDagger() {
        DaggerActivityComponents.builder()
            .networkComponent((application as SortationApplication).getNetworkComponent()).build()
            .inject(this@SelectCancelReason)

        mPresenter.onAttachView(this, this)
    }

    private fun setRadioButton() {
        binding.layoutCancelReason.radioGroup.setOnCheckedChangeListener { group, checkedId ->

            val selected = group.findViewById<RadioButton>(checkedId)

            when (selected.text) {
                getString(R.string.damaged) -> {
                    hideKeyboard()
                    selectReason = getString(R.string.damaged)
                    binding.layoutCancelReason.tvReason.text = getString(R.string.damaged_reason)
                    binding.layoutCancelReason.tvReason.visibility = View.VISIBLE
                    binding.layoutCancelReason.llView.visibility = View.GONE
                    binding.layoutCancelReason.tvPincodeStatus.visibility = View.GONE
                    binding.layoutCancelReason.tvEnterPincode.visibility = View.GONE
                    if (shipmentType == ShipmentType.MPS.name)
                        setupList()
                    else
                        confirmButton(true)
                }
                getString(R.string.lost) -> {
                    hideKeyboard()
                    selectReason = getString(R.string.lost)
                    binding.layoutCancelReason.tvReason.text = getString(R.string.lost_reason)
                    binding.layoutCancelReason.tvReason.visibility = View.VISIBLE
                    binding.layoutCancelReason.llView.visibility = View.GONE
                    binding.layoutCancelReason.tvPincodeStatus.visibility = View.GONE
                    binding.layoutCancelReason.tvEnterPincode.visibility = View.GONE
                    if (shipmentType == ShipmentType.MPS.name)
                        setupList()
                    else
                        confirmButton(true)
                }
                getString(R.string.out_of_serviceable_area) -> {
                    hideKeyboard()
                    selectReason = getString(R.string.out_of_serviceable_area)
                    binding.layoutCancelReason.tvReason.text = getString(R.string.serviceable_reason)
                    binding.layoutCancelReason.tvReason.visibility = View.VISIBLE
                    binding.layoutCancelReason.llView.visibility = View.GONE
                    binding.layoutCancelReason.tvPincodeStatus.visibility = View.GONE
                    binding.layoutCancelReason.tvEnterPincode.visibility = View.GONE
                    binding.layoutCancelReason.rvBoxes.visibility = View.GONE
                    binding.layoutCancelReason.rvHeader.layoutBoxItemHeader.visibility = View.GONE
                    binding.layoutCancelReason.labelSelectBox.visibility = View.GONE
                    selectedChildShipments.clear()
                    confirmButton(true)
                }
                getString(R.string.wrong_pincode) -> {
                    selectReason = getString(R.string.wrong_pincode)
                    binding.layoutCancelReason.tvReason.visibility = View.GONE
                    binding.layoutCancelReason.llView.visibility = View.VISIBLE
                    binding.layoutCancelReason.tvEnterPincode.visibility = View.VISIBLE
                    binding.layoutCancelReason.rvBoxes.visibility = View.GONE
                    binding.layoutCancelReason.rvHeader.layoutBoxItemHeader.visibility = View.GONE
                    binding.layoutCancelReason.labelSelectBox.visibility = View.GONE
                    selectedChildShipments.clear()
                }
            }
        }

    }

    override fun onSuccess(pincodeVerify: PincodeVerify) {
        hideProgress()

        serviceable = pincodeVerify.serviceable
        binding.layoutCancelReason.tvPincodeStatus.visibility = View.VISIBLE
        if (pincodeVerify.serviceable && pincodeVerify.assetName == PreferenceHelper.assignedAssetName) {
            assetId = pincodeVerify.assetId
            assetName = pincodeVerify.assetName
            binding.layoutCancelReason.tvPincodeStatus.text = getString(R.string.serviceable_from_the_existing_dc)
            binding.layoutCancelReason.tvPincodeStatus.setTextColor(green)
            binding.layoutCancelReason.tvPincodeStatus.setBackgroundResource(R.drawable.back_green)
        } else if (pincodeVerify.serviceable) {
            assetId = pincodeVerify.assetId
            assetName = pincodeVerify.assetName
            binding.layoutCancelReason.tvPincodeStatus.text = "Re-route the shipment to $assetName?"
            binding.layoutCancelReason.tvPincodeStatus.setTextColor(green)
            binding.layoutCancelReason.tvPincodeStatus.setBackgroundResource(R.drawable.back_green)
        } else {
            binding.layoutCancelReason.tvPincodeStatus.text = getString(R.string.not_serviceable)
            binding.layoutCancelReason.tvPincodeStatus.setTextColor(red)
            binding.layoutCancelReason.tvPincodeStatus.setBackgroundResource(R.drawable.red_background)
        }

        binding.saveLayout.btnSave.isEnabled = true
        binding.saveLayout.btnSave.setBackgroundResource(R.drawable.blue_button_background)
    }

    override fun onFailure(error: Throwable?) {
        hideProgress()
        processError(error)
    }

    override fun onSubmitSuccess() {
        hideProgress()
        finish()
    }

    override fun onClick(v: View?) {
        when (v?.id) {

            R.id.btn_clear -> finish()

            R.id.btn_save -> {
                showProgress()
                if (!serviceable!!)
                    mPresenter.cancelShipment(selectReason!!, shipmentId!!, selectedChildShipments)
                else
                    mPresenter.updatePincode(
                        binding.layoutCancelReason.edPincode.text.toString(),
                        shipmentId!!,
                        assetId!!,
                        assetName!!
                    )
            }

            R.id.btn_update -> {
                showProgress()
                mPresenter.verifyPincode(binding.layoutCancelReason.edPincode.text.toString(), shipmentId!!)
            }
        }

    }

    private fun confirmButton(enable: Boolean) {
        if (enable) {
            binding.saveLayout.btnSave.isEnabled = true
            binding.saveLayout.btnSave.setBackgroundResource(R.drawable.blue_button_background)
        } else {
            binding.saveLayout.btnSave.isEnabled = false
            binding.saveLayout.btnSave.setBackgroundResource(R.drawable.disable_button)
        }
    }

    override fun afterTextChanged(s: Editable?) {
        if (s?.isEmpty() == true) {
            binding.layoutCancelReason.btnUpdate.isEnabled = false
            binding.layoutCancelReason.btnUpdate.setBackgroundResource(R.drawable.border_blue_rightradius)
            binding.layoutCancelReason.btnUpdate.setBackgroundResource(R.drawable.border_blue_leftradius)

        } else if (s?.length == 6) {
            binding.layoutCancelReason.btnUpdate.isEnabled = true
            binding.layoutCancelReason.btnUpdate.setBackgroundResource(R.drawable.verify_enable_button)
            binding.layoutCancelReason.edPincode.setBackgroundResource(R.drawable.border_blue_leftradius)
        }

        confirmButton(false)
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

    }

    override fun onChildShipmentsFetched(shipments: MutableList<ShipmentDTO>) {
        childShipments.clear()
        childShipments.addAll(shipments)
    }

    private fun setupList() {
        binding.layoutCancelReason.rvHeader.layoutBoxItemHeader.visibility = View.VISIBLE
        binding.layoutCancelReason.rvBoxes.visibility = View.VISIBLE
        binding.layoutCancelReason.labelSelectBox.visibility = View.VISIBLE
        val layoutManager = LinearLayoutManager(this)
        binding.layoutCancelReason.rvBoxes.layoutManager = layoutManager
        binding.layoutCancelReason.rvBoxes.adapter = ChildShipmentAdapter(this, childShipments, this)

        binding.layoutCancelReason.rvBoxes.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                val firstVisible = layoutManager.findFirstVisibleItemPosition()
                if (firstVisible == 0) {
                    binding.layoutCancelReason.tvReferenceId.visibility = View.VISIBLE
                    binding.layoutCancelReason.radioGroup.visibility = View.VISIBLE
                } else if (firstVisible > 1) {
                    binding.layoutCancelReason.tvReferenceId.visibility = View.GONE
                    binding.layoutCancelReason.radioGroup.visibility = View.GONE
                }

            }
        })

        binding.layoutCancelReason.rvHeader.cbSelectAllShipment.setOnCheckedChangeListener { v, isChecked ->
            onSelectOrDeselectAll(isChecked)
        }
    }

    override fun onSelectOrDeselectAll(select: Boolean) {
        selectedChildShipments.clear()
        childShipments.forEach { it.selected = select }
        if (select) {
            selectedChildShipments.addAll(childShipments)
        } else {
            selectedChildShipments.removeAll(childShipments)
        }
        binding.layoutCancelReason.rvBoxes.adapter?.notifyDataSetChanged()
        confirmButton(selectedChildShipments.isNotEmpty())
    }

    override fun onSelectOrDeselectItem(select: Boolean, position: Int) {
        childShipments[position].selected = select
        if (select)
            selectedChildShipments.add(childShipments[position])
        else
            selectedChildShipments.remove(childShipments[position])
        binding.layoutCancelReason.rvBoxes.adapter?.notifyDataSetChanged()
        confirmButton(selectedChildShipments.isNotEmpty())
    }
}