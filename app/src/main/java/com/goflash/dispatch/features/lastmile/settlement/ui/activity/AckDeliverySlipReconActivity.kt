package com.goflash.dispatch.features.lastmile.settlement.ui.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.view.View
import androidx.annotation.RequiresApi
import androidx.camera.core.TorchState
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.goflash.dispatch.R
import com.goflash.dispatch.SortationApplication
import com.goflash.dispatch.app_constants.CAMERA_REQ
import com.goflash.dispatch.app_constants.sprinter_name
import com.goflash.dispatch.app_constants.trip_id
import com.goflash.dispatch.data.PoaResponseForRecon
import com.goflash.dispatch.databinding.LayoutAckDeliverySlipReconBinding
import com.goflash.dispatch.features.lastmile.settlement.AcknowledgeSlipViewModel
import com.goflash.dispatch.features.lastmile.settlement.ui.adapter.AckDeliverySlipAdapter
import com.goflash.dispatch.listeners.BarcodeScannerInterface
import com.goflash.dispatch.ui.activity.ScannerBaseActivity
import com.goflash.dispatch.util.observe
import com.pharmeasy.barcode.BarcodeReader
import com.pharmeasy.barcode.Event
import com.pharmeasy.barcode.ModeSelectedListener
import com.pharmeasy.barcode.ScannerType

class AckDeliverySlipReconActivity : ScannerBaseActivity(), View.OnClickListener,
    BarcodeScannerInterface {

    private lateinit var binding: LayoutAckDeliverySlipReconBinding

    private var modeSelected = false

    private var tripId: Long = 0

    private var sprinter: String? = null

    private var barcodeScanned = false



    private lateinit var acknowledgeSlipViewModel: AcknowledgeSlipViewModel

    val listener = object : ModeSelectedListener {
        override fun onModeSelected(mode: String) {
            modeSelected = true
            when (mode) {
                ScannerType.BLUETOOTH_SCANNER.displayName -> {
                    binding.scanContentLayout.scanContent.scanContent.visibility = View.VISIBLE
                    binding.scanContentLayout.scanContentCamera.scanTask.visibility = View.GONE
                    binding.scanContentLayout.etScan.visibility = View.GONE
                }

                ScannerType.OTG_SCANNER.displayName -> {
                    binding.scanContentLayout.scanContent.scanContent.visibility = View.VISIBLE
                    binding.scanContentLayout.scanContentCamera.scanTask.visibility = View.GONE
                    binding.scanContentLayout.etScan.visibility = View.VISIBLE
                    binding.scanContentLayout.etScan.requestFocus()
                }

                ScannerType.CAMERA_SCANNER.displayName -> {
                    binding.scanContentLayout.scanContent.scanContent.visibility = View.GONE
                    binding.scanContentLayout.scanContentCamera.scanTask.visibility = View.VISIBLE
                    binding.scanContentLayout.etScan.visibility = View.GONE
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LayoutAckDeliverySlipReconBinding.inflate(layoutInflater)
        setContentView(binding.root)

        acknowledgeSlipViewModel = ViewModelProvider(this).get(AcknowledgeSlipViewModel::class.java)

        if (intent?.hasExtra(trip_id) == true) {
            tripId = intent.getLongExtra(trip_id, 0)
        } else if (intent?.hasExtra("tripId") == true) {
            tripId = intent.getStringExtra("tripId")?.toLong() ?: 0
        }

        sprinter = intent.getStringExtra(sprinter_name)

        initializeObservers()
        acknowledgeSlipViewModel.initialize(tripId)

        initViews()
        initScanner()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermissions()
        }

    }

    override fun initScanner() {
        initBarcodeScanner(this)
        if (!SortationApplication.getSortationApplicationClass().getBarcodeReader().UIView!!) {

            SortationApplication.getSortationApplicationClass().getBarcodeReader()
                .initializeScanner(this, intent, R.id.zxing_scanner_view, binding.scanContentLayout.etScan, listener)

            (application as SortationApplication).getBarcodeReader().setTorchListener { state ->
                if (state == TorchState.ON) {
                    binding.scanContentLayout.scanContentCamera.imgFlashlightOn.setImageResource(
                        R.drawable.ic_flash_off_black_24dp
                    )
                } else {
                    binding.scanContentLayout.scanContentCamera.imgFlashlightOn.setImageResource(
                        R.drawable.ic_flash_on_black_24dp
                    )
                }
            }
        }

        binding.scanContentLayout.etScan.inputType = InputType.TYPE_NULL


        val barcodeObserver = Observer<Event<String>> {
            it.getContentIfNotHandled()?.let { i ->
                onBarcodeScanned(i.trim())
            }
        }

        BarcodeReader.barcodeData.observe(this, barcodeObserver)
    }

    private fun initializeObservers() {
        observe(acknowledgeSlipViewModel.ackSlips, ::onAckSlipsFetched)
        observe(acknowledgeSlipViewModel.errorMessage, ::showError)
        observe(acknowledgeSlipViewModel.allowNext, ::enableProceed)
    }

    private fun initViews() {
        binding.toolBar.toolbarTitle.text = getString(R.string.acknowledge_delivery_slip)
        binding.toolBar.tvSprinter.text =
            String.format(getString(R.string.trip_id_with_sprinter), tripId, sprinter)

        binding.btnPaymentLayout.btnPayment.text = getString(R.string.proceed)

        binding.labelShipmentList.text = getString(R.string.scanned_slips)

        binding.rvShipments.layoutManager = LinearLayoutManager(this)
        binding.toolBar.ivBack.setOnClickListener(this)
        binding.btnPaymentLayout.btnPayment.setOnClickListener(this)
        binding.scanContentLayout.scanContentCamera.imgFlashlightOn.setOnClickListener(this)
        binding.scanContentLayout.scanContentCamera.imgFlashlightOff.setOnClickListener(this)
        binding.scanContentLayout.scanContent.imgScanner.setOnClickListener(this)
        binding.scanContentLayout.scanContentCamera.imgScannerCamera.setOnClickListener(this)

    }

    override fun onBarcodeScanned(barcode: String) {
        acknowledgeSlipViewModel.onBarcodeScanned(barcode)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_payment -> {
                val intent = Intent(this, Step3CashCollectionActivity::class.java)
                intent.putExtra("tripId", tripId.toString())
                intent.putExtra(sprinter_name, sprinter)
                startActivity(intent)
            }
        }

    }

    private fun onAckSlipsFetched(ackSlips: List<PoaResponseForRecon>) {
        val adapter = AckDeliverySlipAdapter(this, ackSlips)
        binding.rvShipments.layoutManager = LinearLayoutManager(this)
        binding.rvShipments.adapter = adapter

        val scannedCount = ackSlips.filter { it.isScanned }.size
        binding.tvTaskCount.text = "$scannedCount / ${ackSlips.size}"
    }

    private fun showError(errorMessage: String) {
        errorMessage(errorMessage)
    }

    private fun enableProceed(enable: Boolean) {
        with(binding.btnPaymentLayout.btnPayment) {
            this.isEnabled = true
            this.setBackgroundResource(if (enable) R.drawable.blue_button_background else R.drawable.grey_button_background)
        }
    }

    /**
     * Method to check permission for camera and storage, put in login time due to need at multiple places so asked in advance
     * */
    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkPermissions(): Boolean {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.CAMERA
                ),
                CAMERA_REQ
            )
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            CAMERA_REQ -> {
                // If request is CANCELLED, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    if (!SortationApplication.getSortationApplicationClass()
                            .getBarcodeReader().UIView!!
                    ) {
                        SortationApplication.getSortationApplicationClass().getBarcodeReader()
                            .onResume()
                        //barcodeView?.resume()
                    } else
                        SortationApplication.getSortationApplicationClass().getBarcodeReader()
                            .registerBroadcast(
                                this
                            )
                }
                return
            }
        }
    }
}