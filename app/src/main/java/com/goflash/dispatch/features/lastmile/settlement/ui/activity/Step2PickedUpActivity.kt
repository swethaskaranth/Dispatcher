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
import androidx.recyclerview.widget.LinearLayoutManager
import com.goflash.dispatch.R
import com.goflash.dispatch.SortationApplication
import com.goflash.dispatch.app_constants.*
import com.goflash.dispatch.databinding.LayoutSettlelmentScanShipmentBinding
import com.goflash.dispatch.di.components.DaggerActivityComponents
import com.goflash.dispatch.features.lastmile.settlement.presenter.SettlementScanShipmentPresenter
import com.goflash.dispatch.features.lastmile.settlement.ui.adapter.ShipmentAdapter
import com.goflash.dispatch.features.lastmile.settlement.view.SettlementScanShipmentView
import com.goflash.dispatch.listeners.BarcodeScannerInterface
import com.goflash.dispatch.ui.activity.ScannerBaseActivity
import com.pharmeasy.barcode.BarcodeReader
import com.pharmeasy.barcode.Event
import com.pharmeasy.barcode.ModeSelectedListener
import com.pharmeasy.barcode.ScannerType
import javax.inject.Inject

class Step2PickedUpActivity : ScannerBaseActivity(), SettlementScanShipmentView, View.OnClickListener,
    BarcodeScannerInterface {

    @Inject
    lateinit var mPresenter: SettlementScanShipmentPresenter

    private lateinit var binding: LayoutSettlelmentScanShipmentBinding

    private var modeSelected = false

    private var tripId: Long? = null

    private var sprinter: String? = null

    private var barcodeScanned = false

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
                    //barcodeView?.setTorchListener(this@Step2PickedUpActivity)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LayoutSettlelmentScanShipmentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if(intent?.hasExtra(trip_id) == true){
            tripId = intent.getLongExtra(trip_id, 0)
        }else if (intent?.hasExtra("tripId") == true){
            tripId = intent.getStringExtra("tripId")?.toLong()
        }

        sprinter = intent.getStringExtra(sprinter_name)

        initScanner()
        initDagger()
        initViews()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermissions()
        }
    }


    private fun initViews() {
        binding.toolBar.toolbarTitle.text = getString(R.string.received_picked)
        binding.toolBar.tvSprinter.text = String.format(getString(R.string.trip_id_with_sprinter), tripId, sprinter)

        binding.btnPaymentLayout.btnPayment.text = getString(R.string.proceed)

        binding.rvShipments.layoutManager = LinearLayoutManager(this)
        binding.toolBar.ivBack.setOnClickListener(this)
        binding.btnPaymentLayout.btnPayment.setOnClickListener(this)
        binding.scanContentLayout.scanContentCamera.imgFlashlightOn.setOnClickListener(this)
        binding.scanContentLayout.scanContentCamera.imgFlashlightOff.setOnClickListener(this)
        binding.scanContentLayout.scanContent.imgScanner.setOnClickListener(this)
        binding.scanContentLayout.scanContentCamera.imgScannerCamera.setOnClickListener(this)

        mPresenter.getShipments()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        if (intent?.getBooleanExtra(refresh, false) == true)
            mPresenter.getShipments()

        barcodeScanned = false
    }

    private fun initDagger() {
        DaggerActivityComponents.builder()
            .networkComponent((application as SortationApplication).getNetworkComponent())
            .build().inject(this@Step2PickedUpActivity)

        mPresenter.onAttachView(this, this)

        mPresenter.setTripId(tripId!!)

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

    override fun onResume() {
        super.onResume()

        barcodeScanned = false

    }

    override fun onPause() {

        if (!modeSelected)
            SortationApplication.getSortationApplicationClass().getBarcodeReader().clearMode()

        super.onPause()
    }

    override fun onShipmentsFetched(list: List<String>) {
        binding.rvShipments.adapter = ShipmentAdapter(this, list)
    }

    override fun setShipmentCount(scanned: Int, total: Int) {
        binding.tvTaskCount.text = "$scanned/$total"
    }

    override fun onFailure(error: Throwable?) {
        hideProgress()
        processError(error)
        barcodeScanned = false
    }


    override fun onBarcodeScanned(barcode: String) {
        if (!barcodeScanned) {
            barcodeScanned = true
            showProgress()
            mPresenter.onBarcodeScanned(barcode, tripId!!.toLong())
        }
    }

    override fun takeToScanActivity(shipmentId: String, refId: String, partialDelivery : Boolean) {
        hideProgress()

        val intent = Intent(this, ScanReturnItemActivity::class.java)
        intent.putExtra(shipment_id, shipmentId)
        intent.putExtra(trip_id, tripId)
        intent.putExtra(sprinter_name, sprinter)
        intent.putExtra(reference_id, refId)
        intent.putExtra(partial_delivery, partialDelivery)

        startActivity(intent)
    }

    override fun takeToReviewActivity(shipmentId: String, refId: String, partialDelivery : Boolean) {
        hideProgress()

        val intent = Intent(this, ReceiveItemActivity::class.java)
        intent.putExtra(shipment_id, shipmentId)
        intent.putExtra(trip_id, tripId)
        intent.putExtra(sprinter_name, sprinter)
        intent.putExtra(reference_id, refId)
        intent.putExtra(partial_delivery, partialDelivery)
        startActivity(intent)
    }

    override fun takeToItemSummaryActivity(
        shipmentId: String,
        refId: String,
        partialDelivery: Boolean
    ) {
        hideProgress()

        val intent = Intent(this, ItemSummaryActivity::class.java)
        intent.putExtra(shipment_id, shipmentId)
        intent.putExtra(trip_id, tripId)
        intent.putExtra(sprinter_name, sprinter)
        intent.putExtra(reference_id, refId)
        intent.putExtra(partial_delivery, partialDelivery)
        startActivity(intent)
    }

    override fun enableOrDisableProceed(enable: Boolean) {
        binding.btnPaymentLayout.btnPayment.isEnabled = if (enable) {
            binding.btnPaymentLayout.btnPayment.setBackgroundResource(R.drawable.blue_button_background)
            true
        } else {
            binding.btnPaymentLayout.btnPayment.setBackgroundResource(R.drawable.grey_button_background)
            false
        }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.ivBack -> finish()
            R.id.btn_payment -> mPresenter.onNext(tripId!!)
            R.id.img_flashlight_on -> {
                (application as SortationApplication).getBarcodeReader().toggleTorch()
            }
            R.id.img_scanner -> {
                SortationApplication.getSortationApplicationClass().getBarcodeReader()
                    .selectScanner(this, intent, R.id.zxing_scanner_view, binding.scanContentLayout.etScan, listener)
            }
            R.id.img_scanner_camera -> {
                SortationApplication.getSortationApplicationClass().getBarcodeReader()
                    .selectScanner(this, intent, R.id.zxing_scanner_view, binding.scanContentLayout.etScan, listener)
            }
        }
    }

    /*override fun onTorchOn() {
        img_flashlight_on.visibility = View.GONE
        img_flashlight_off.visibility = View.VISIBLE
    }

    override fun onTorchOff() {
        img_flashlight_off.visibility = View.GONE
        img_flashlight_on.visibility = View.VISIBLE
    }*/

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
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
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

    override fun startStep3CashActivity() {
        val intent = Intent(this, Step3CashCollectionActivity::class.java)
        intent.putExtra("tripId",tripId.toString())
        intent.putExtra(sprinter_name,sprinter)
        startActivity(intent)
    }

    override fun startReceiveFmPickupActivity() {
        val intent = Intent(this, ReceiveFmPickupShipmentActivity::class.java)
        intent.putExtra("tripId",tripId.toString())
        intent.putExtra(sprinter_name,sprinter)
        startActivity(intent)
    }

    override fun startVerifyImageActivity() {
        val intent = Intent(this, Step4VerifyImagesActivity::class.java)
        intent.putExtra("tripId",tripId.toString())
        intent.putExtra(sprinter_name,sprinter)
        startActivity(intent)
    }

    override fun startAckDeliverySlipReconActivity() {
        val intent = Intent(this, AckDeliverySlipReconActivity::class.java)
        intent.putExtra("tripId", tripId.toString())
        intent.putExtra(sprinter_name, sprinter)
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        mPresenter.onDetachView()
    }
}