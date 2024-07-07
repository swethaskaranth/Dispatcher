package com.goflash.dispatch.features.lastmile.settlement.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import co.uk.rushorm.core.RushSearch
import com.goflash.dispatch.R
import com.goflash.dispatch.data.*
import com.goflash.dispatch.databinding.ActivityStep1UnscannedBinding
import com.goflash.dispatch.features.lastmile.settlement.listeners.SelectedListener
import com.goflash.dispatch.features.lastmile.settlement.ui.adapter.UnScannedItemAdapter
import com.goflash.dispatch.features.lastmile.settlement.ui.adapter.UndeliveredMPSShipmentsAdapter
import com.goflash.dispatch.ui.activity.BaseActivity
import org.jetbrains.anko.toast

class Step1UnscannedActivity : BaseActivity(), View.OnClickListener, SelectedListener {

    private var tripId: String? = null
    private var sprinterName: String? = null

    private var mList: MutableList<UndeliveredShipmentDTO> = mutableListOf()
    private var tripList: TripSettlementDTO? = null

    private lateinit var binding: ActivityStep1UnscannedBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStep1UnscannedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
    }

    private fun initViews() {

        tripId = intent.getStringExtra("tripId")
        sprinterName = intent.getStringExtra("sprinterName")

        tripList = getUndeliveredData(tripId!!.toString())

        binding.btnPaymentLayout.btnPayment.setOnClickListener(this)
        binding.toolBar.ivBack.setOnClickListener(this)

        binding.toolBar.toolbarTitle.text = getString(R.string.received_undelivered)
        binding.btnPaymentLayout.btnPayment.text = resources.getString(R.string.proceed)
        binding.toolBar.tvSprinter.text = "Trip ID #$tripId - $sprinterName"

        mList = RushSearch()
            .whereChildOf(TripSettlementDTO::class.java, "undeliveredShipment", tripList?.id).and()
            .whereEqual("scanned", false)
            .find(UndeliveredShipmentDTO::class.java)

        if(mList.isEmpty()){
            binding.selectReason.visibility = View.GONE
        }

        binding.rvTasks.layoutManager = LinearLayoutManager(this)
        binding.rvTasks.adapter =  UnScannedItemAdapter(this, mList, this)

        val undeliveredMPS = RushSearch()
            .whereChildOf(TripSettlementDTO::class.java, "lostDamagedShipments", tripList?.id)
            .find(LostDamagedShipment::class.java)

        if(undeliveredMPS.isNotEmpty()){
            binding.shipmentsLost.visibility = View.VISIBLE
            binding.rvdamagedAndLost.visibility = View.VISIBLE
            binding.rvdamagedAndLost.layoutManager = LinearLayoutManager(this)
            binding.rvdamagedAndLost.adapter = UndeliveredMPSShipmentsAdapter(this,undeliveredMPS)
        }

    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.btn_payment -> showStep2()
            R.id.ivBack -> finish()
        }
    }

    override fun onShipmentSelected(position: Int, data: UndeliveredShipmentDTO) {
        val saveData = RushSearch()
            .whereChildOf(TripSettlementDTO::class.java, "undeliveredShipment", tripList?.id).and()
            .whereEqual("scanned", false).and().whereEqual("shipmentId", data.shipmentId)
            .findSingle(UndeliveredShipmentDTO::class.java)

        saveData.reason = data.reason
        saveData.status = "Pending"
        saveData.save()

    }

    private fun getUndeliveredData(tripId: String): TripSettlementDTO {
        return RushSearch().whereEqual("tripId", tripId.toLong()).findSingle(TripSettlementDTO::class.java)
    }

    private fun showStep2(){

        val saveData = RushSearch()
            .whereChildOf(TripSettlementDTO::class.java, "undeliveredShipment", tripList?.id).and()
            .whereEqual("scanned", false)
            .find(UndeliveredShipmentDTO::class.java)

        if(saveData.any { it.reason.isNullOrEmpty() }) {
            toast(getString(R.string.unscanned_shipment))
            return
        }

        val trip = RushSearch().whereEqual("tripId", tripId!!.toLong()).findSingle(TripSettlementDTO::class.java)
        if(trip != null){
            trip.isUndeliveredScanned = true
            trip.save()
        }

        val intent = if(tripList?.returnShipment!!.isEmpty()) {
            val fmShipments = RushSearch().whereEqual("tripId", tripId!!.toLong()).find(FmPickedShipment::class.java)
            if (fmShipments.isNullOrEmpty()){
                val ackslips = RushSearch().whereEqual("tripId", tripId!!.toLong()).find(AckSlipDto::class.java)
                if(ackslips.isNullOrEmpty()) {
                    val deliverySlips = RushSearch().whereChildOf(
                        TripSettlementDTO::class.java, "poas",
                        trip!!.id
                    )
                        .find(PoaResponseForRecon::class.java)
                    if(deliverySlips.isNullOrEmpty())
                        Intent(this, Step3CashCollectionActivity::class.java)
                    else
                        Intent(this, AckDeliverySlipReconActivity::class.java)
                }
                else
                    Intent(this, Step4VerifyImagesActivity::class.java)
            }
            else
                Intent(this, ReceiveFmPickupShipmentActivity::class.java)
        }else
            Intent(this, Step2PickedUpActivity::class.java)

        intent.putExtra("tripId", tripId)
        intent.putExtra("sprinterName", sprinterName)
        startActivity(intent)
    }
}
