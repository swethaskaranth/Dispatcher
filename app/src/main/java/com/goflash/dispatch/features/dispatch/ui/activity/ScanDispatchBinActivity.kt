package com.goflash.dispatch.features.dispatch.ui.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.Toast
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
import com.goflash.dispatch.app_constants.isDispatchable
import com.goflash.dispatch.app_constants.single_order
import com.goflash.dispatch.data.PackageDto
import com.goflash.dispatch.data.ScannedOrder
import com.goflash.dispatch.databinding.ActivityDispatchScanBinBinding
import com.goflash.dispatch.di.components.DaggerActivityComponents
import com.goflash.dispatch.features.dispatch.presenter.ScanDispatchBinPresenter
import com.goflash.dispatch.features.dispatch.view.ScanDispatchBinView
import com.goflash.dispatch.listeners.BarcodeScannerInterface
import com.goflash.dispatch.ui.activity.CancelledActivity
import com.goflash.dispatch.ui.activity.OrdersListActivity
import com.goflash.dispatch.ui.activity.ScannerBaseActivity
import com.goflash.dispatch.util.PreferenceHelper
import com.goflash.dispatch.util.hasFlash
import com.pharmeasy.barcode.BarcodeReader
import com.pharmeasy.barcode.Event
import com.pharmeasy.barcode.ModeSelectedListener
import com.pharmeasy.barcode.ScannerType
import javax.inject.Inject


class ScanDispatchBinActivity : ScannerBaseActivity(), ScanDispatchBinView, View.OnClickListener,
    BarcodeScannerInterface {

    @Inject
    lateinit var presenter: ScanDispatchBinPresenter

    private var packageDto: PackageDto? = null

    private val scannedOrders: ArrayList<ScannedOrder> = ArrayList()

    private lateinit var binding: ActivityDispatchScanBinBinding

    var dispatchable = true

    private var modeSelected = false

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
                    binding.scanContent. etScan.requestFocus()
                }
                ScannerType.CAMERA_SCANNER.displayName -> {
                    binding.scanContent.scanContent.scanContent.visibility = View.GONE
                    binding.scanContent.scanContentCamera.scanTask.visibility = View.VISIBLE
                    binding.scanContent.etScan.visibility = View.GONE
                    //barcodeView?.setTorchListener(this@ScanDispatchBinActivity)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDispatchScanBinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dispatchable = intent.getBooleanExtra(isDispatchable, true)

        // dispatchOrShowCancelledOrders()

        initScanner()
        initDagger()
        initViews()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermissions()
        }

    }

    private fun initDagger() {
        DaggerActivityComponents.builder().networkComponent((application as SortationApplication).getNetworkComponent())
                .build().inject(this@ScanDispatchBinActivity)

        presenter.onAttachView(this@ScanDispatchBinActivity, this)

    }

    private fun initViews() {
        val toolbar = findViewById<View>(R.id.toolBar1) as Toolbar
        setSupportActionBar(toolbar)

        binding.toolBar1.toolbarTitle.text = getString(R.string.scan_bin_delivery_label)

        binding.scanContent.scanContent.scanLabel.text = getString(R.string.scan_bin_delivery_label)

        val actionbar: ActionBar? = supportActionBar
        actionbar?.apply {
            setDisplayHomeAsUpEnabled(false)
            setDisplayShowTitleEnabled(false)
        }

        binding.orderDetail.itemLabel.text = getString(R.string.bin_no)

        binding.chemistName.text = PreferenceHelper.assignedAssetName

        presenter.getPackage()


        if (dispatchable)
            binding.proceedBtn.text = getString(R.string.dispatch)
        else
            binding.proceedBtn.text = getString(R.string.proceed)


        binding.proceedBtn.setOnClickListener(this)
        binding.scanCount.tvViewDetails.setOnClickListener(this)

        if (!hasFlash(applicationContext)) {
            binding.scanContent.scanContentCamera.imgFlashlightOn.visibility = View.GONE
        }

        binding.scanContent.scanContentCamera.imgFlashlightOn.setOnClickListener(this)
        binding.scanContent.scanContentCamera.imgFlashlightOff.setOnClickListener(this)
        binding.scanContent.scanContent.imgScanner.setOnClickListener(this)
        binding.scanContent.scanContentCamera.imgScannerCamera.setOnClickListener(this)

    }

    override fun initScanner() {
        initBarcodeScanner(this)
        if (!SortationApplication.getSortationApplicationClass().getBarcodeReader().UIView!!) {

            SortationApplication.getSortationApplicationClass().getBarcodeReader()
                .initializeScanner(this, intent, R.id.zxing_scanner_view,binding.scanContent.etScan,listener)

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
        }else{
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



    override fun onPause() {


            if(!modeSelected)
                SortationApplication.getSortationApplicationClass().getBarcodeReader().clearMode()

        super.onPause()
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
                   if (!SortationApplication.getSortationApplicationClass().getBarcodeReader().UIView!!) {
           SortationApplication.getSortationApplicationClass().getBarcodeReader().onResume()
           //barcodeView?.resume()
       }
                    else
                        SortationApplication.getSortationApplicationClass().getBarcodeReader().registerBroadcast(
                            this
                        )
                }
                return
            }
        }
    }


    override fun onPackageFetched(packageDto: PackageDto) {

        this.packageDto = packageDto

        scannedOrders.clear()
        scannedOrders.addAll(this.packageDto!!.scannedOrders)

        binding.orderDetail.orderId.text = scannedOrders.get(0).binNumber
        binding.scanCount.itemCount.text = scannedOrders.size.toString()

        if (this.packageDto!!.isFurtherScannable)
            binding.scanContent.scanContent.scanContent.visibility = View.VISIBLE
        else
            binding.scanContent.scanContent.scanContent.visibility = View.GONE
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.proceed_btn -> dispatchOrShowCancelledOrders()
            R.id.tv_view_details -> taketoOrderListScreen()
            R.id.img_flashlight_on ->{
                (application as SortationApplication).getBarcodeReader().toggleTorch()
            }

            R.id.img_scanner ->{
                SortationApplication.getSortationApplicationClass().getBarcodeReader().selectScanner(this, intent, R.id.zxing_scanner_view, binding.scanContent.etScan, listener)
            }
            R.id.img_scanner_camera -> {
                SortationApplication.getSortationApplicationClass().getBarcodeReader().selectScanner(this, intent, R.id.zxing_scanner_view, binding.scanContent.etScan, listener)
            }
        }
    }


    override fun onBarcodeScanned(barcode: String) {
        if (!packageDto!!.isFurtherScannable)
            return

        presenter.initiateDispatch(barcode)

    }

    private fun dispatchOrShowCancelledOrders() {
        if (dispatchable) {
            showProgress()
            presenter.dispatchShipments(scannedOrders)

        } else
            takeToCancelledOrderScreen()

    }

    private fun taketoOrderListScreen() {
        val intent = Intent(this@ScanDispatchBinActivity, OrdersListActivity::class.java)
        // intent.putExtra(package_status,scannedOrders.get(0))
        startActivity(intent)
    }

    private fun takeToCancelledOrderScreen() {
        val intent = Intent(this@ScanDispatchBinActivity, CancelledActivity::class.java)
        // intent.putExtra(package_status,scannedOrders.get(0))
        startActivity(intent)

    }

    override fun onBackPressed() {
        showToast(this, getString(R.string.cannot_go_back))
    }

    override fun onSuccess(message: String) {
        hideProgress()
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        finish()

    }

    override fun dispatchCancelledOrdersPresent() {
        hideProgress()
        showToast(this,getString(R.string.cancelled_orders_present))

        dispatchable = false

        binding.proceedBtn.text = getString(R.string.proceed)

    }

    override fun onFailure(error: Throwable?) {
        hideProgress()
        processError(error)
    }

    override fun onSuccess(isDispatchable: Boolean, singleOrderScanned : Boolean) {
        hideProgress()
        if (!isDispatchable) {
            val intent = Intent(this, CancelledActivity::class.java)
            intent.putExtra(single_order,singleOrderScanned)
            startActivity(intent)
        } else {
            presenter.getPackage()
        }
    }

    /*override fun onData(scanDataCollection: ScanDataCollection?) {
        if (scanDataCollection == null)
            return
        if (scanDataCollection.result != ScannerResults.SUCCESS)
            return
        val scanData = scanDataCollection.scanData
        for (data in scanData) {
            val dataString = data.data

            runOnUiThread {
                onBarcodeScanned(dataString.trim())
            }
        }
    }*/

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDetachView()
    }

}