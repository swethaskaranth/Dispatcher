package com.goflash.dispatch.features.rtoReceiving.ui.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.Toolbar
import androidx.camera.core.TorchState
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.goflash.dispatch.R
import com.goflash.dispatch.SortationApplication
import com.goflash.dispatch.app_constants.CAMERA_REQ
import com.goflash.dispatch.data.PackageDto
import com.goflash.dispatch.data.ReceivingShipmentDTO
import com.goflash.dispatch.databinding.ActivityScanSortBinBinding
import com.goflash.dispatch.di.components.DaggerActivityComponents
import com.goflash.dispatch.features.rtoReceiving.presenter.ScanBinPresenter
import com.goflash.dispatch.features.rtoReceiving.view.ScanBinView
import com.goflash.dispatch.listeners.BarcodeScannerInterface
import com.goflash.dispatch.ui.activity.ScannerBaseActivity
import com.pharmeasy.barcode.BarcodeReader
import com.pharmeasy.barcode.Event
import com.pharmeasy.barcode.ModeSelectedListener
import com.pharmeasy.barcode.ScannerType
import javax.inject.Inject

class ScanBinActivity : ScannerBaseActivity(), ScanBinView, BarcodeScannerInterface {

    @Inject
    lateinit var presenter: ScanBinPresenter

    private var modeSelected = false

    private lateinit var binding: ActivityScanSortBinBinding

    val listener = object : ModeSelectedListener {
        override fun onModeSelected(mode: String) {
            modeSelected = true
            when (mode) {
                ScannerType.BLUETOOTH_SCANNER.displayName -> {
                    binding.scanContent.scanContent.scanContent.visibility = View.VISIBLE
                    binding.scanContent.scanContentCamera.scanTask.visibility = View.GONE
                    binding.scanContent.etScan.visibility = View.GONE
                }
                ScannerType.OTG_SCANNER.displayName -> {
                    binding.scanContent.scanContent.scanContent.visibility = View.VISIBLE
                    binding.scanContent.scanContentCamera.scanTask.visibility = View.GONE
                    binding.scanContent.etScan.visibility = View.VISIBLE
                    binding.scanContent.etScan.requestFocus()
                }
                ScannerType.CAMERA_SCANNER.displayName -> {
                    binding.scanContent.scanContent.scanContent.visibility = View.GONE
                    binding.scanContent.scanContentCamera.scanTask.visibility = View.VISIBLE
                    binding.scanContent.etScan.visibility = View.GONE
                    //barcodeView?.setTorchListener(this@ScanBinActivity)
                }
            }
        }
    }

    private var binNumber: String = ""
    private var packageDTO: PackageDto? = null
    private var shipment: ReceivingShipmentDTO? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanSortBinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        packageDTO = intent?.getParcelableExtra("scannedPackage")
        shipment = intent?.getSerializableExtra("shipment") as ReceivingShipmentDTO?

        binNumber = packageDTO?.scannedOrders?.get(0)?.binNumber ?: ""

        initScanner()
        initViews()
        initDagger()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermissions()
        }

    }

    private fun initViews() {
        binding.scanContent.binName.text = binNumber
        binding.orderDetail.orderId.text = shipment?.referenceId
        binding.scanContent.tvMessage.visibility = View.VISIBLE
        binding.scanContent.binName.visibility = View.VISIBLE
        binding.scanContent.closeBag.visibility = View.GONE

        val toolbar = findViewById<View>(R.id.toolBar1) as Toolbar
        setSupportActionBar(toolbar)

        val actionbar: ActionBar? = supportActionBar
        actionbar?.apply {
            setDisplayHomeAsUpEnabled(false)
            setDisplayShowTitleEnabled(false)
        }

        binding.orderDetail.orderDetail.visibility = View.VISIBLE

        binding.toolBar1.toolbarTitle.text = getString(R.string.scan_bin)
        binding.scanContent.scanDeliveryLabel.text = getString(R.string.scan_bin_number)
        binding.scanContent.scanContent.scanLabel.text = getString(R.string.scan_bin)
    }

    private fun initDagger() {
        DaggerActivityComponents.builder()
            .networkComponent((application as SortationApplication).getNetworkComponent())
            .build().inject(this@ScanBinActivity)

        presenter.onAttach(this, this)
    }

    override fun initScanner() {
        initBarcodeScanner(this)
        if (!SortationApplication.getSortationApplicationClass().getBarcodeReader().UIView!!) {

            SortationApplication.getSortationApplicationClass().getBarcodeReader()
                .initializeScanner(this, intent, R.id.zxing_scanner_view, binding.scanContent.etScan, listener)

            (application as SortationApplication).getBarcodeReader().setTorchListener { state ->
                if (state == TorchState.ON) {
                    binding.scanContent.scanContentCamera.imgFlashlightOn.setImageResource(
                        R.drawable.ic_flash_off_black_24dp
                    )
                } else {
                    binding.scanContent.scanContentCamera.imgFlashlightOn.setImageResource(
                        R.drawable.ic_flash_on_black_24dp
                    )
                }
            }

            binding.scanContent.scanContentCamera.imgFlashlightOn.setOnClickListener {
                (application as SortationApplication).getBarcodeReader().toggleTorch()
            }

        } else {
            binding.scanContent.scanContent.imgScanner.visibility = View.GONE
        }

        binding.scanContent.etScan.inputType = InputType.TYPE_NULL


        val barcodeObserver = Observer<Event<String>> {
            it.getContentIfNotHandled()?.let { i ->
                onBarcodeScanned(i.trim())
            }
        }

        BarcodeReader.barcodeData.observe(this, barcodeObserver)
    }

    override fun onBarcodeScanned(barcode: String) {
        showProgress()
        if(barcode == packageDTO?.scannedOrders?.get(0)?.binNumber)
            presenter.onBinScan(barcode, packageDTO!!)
        else
            onFailure(Throwable("Invalid bin"))
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDetach()
    }

   /* override fun onTorchOn() {
        img_flashlight_on.visibility = View.GONE
        img_flashlight_off.visibility = View.VISIBLE

    }

    override fun onTorchOff() {
        img_flashlight_off.visibility = View.GONE
        img_flashlight_on.visibility = View.VISIBLE
    }*/

    /**
     * Check if the device's camera has a Flashlight.
     * @return true if there is Flashlight, otherwise false.
     */
    private fun hasFlash(): Boolean {
        return applicationContext.packageManager
            .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)
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

    override fun onPause() {

        if (!modeSelected)
            SortationApplication.getSortationApplicationClass().getBarcodeReader().clearMode()

        super.onPause()
    }

    override fun onFailure(error: Throwable?) {
        hideProgress()
        processError(error)
    }

    override fun onSuccessBinScan() {
        hideProgress()
        val intent = Intent()
        intent.putExtra("shipment", shipment)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    override fun onBackPressed() {
        showToast(this, getString(R.string.cannot_go_back))
    }
}