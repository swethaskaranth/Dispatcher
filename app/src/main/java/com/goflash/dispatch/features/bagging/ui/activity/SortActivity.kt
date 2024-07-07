package com.goflash.dispatch.features.bagging.ui.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.text.InputType
import android.view.MenuItem
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.Toolbar
import androidx.camera.core.TorchState
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.goflash.dispatch.R
import com.goflash.dispatch.SortationApplication
import com.goflash.dispatch.app_constants.CAMERA_REQ
import com.goflash.dispatch.app_constants.cancelled
import com.goflash.dispatch.app_constants.close_Bag
import com.goflash.dispatch.app_constants.scannedPackage
import com.goflash.dispatch.data.PackageDto
import com.goflash.dispatch.data.ScannedOrder
import com.goflash.dispatch.databinding.ActivityScanSortBinBinding
import com.goflash.dispatch.di.components.DaggerActivityComponents
import com.goflash.dispatch.features.bagging.presenter.SortPresenter
import com.goflash.dispatch.features.bagging.ui.adapter.ScannedOrdersAdapter
import com.goflash.dispatch.features.bagging.view.SortationView
import com.goflash.dispatch.listeners.BarcodeScannerInterface
import com.goflash.dispatch.ui.activity.CancelledActivity
import com.goflash.dispatch.ui.activity.ScannerBaseActivity
import com.goflash.dispatch.util.FixedSizeList
import com.goflash.dispatch.util.PreferenceHelper
import com.goflash.dispatch.util.Toaster
import com.pharmeasy.barcode.BarcodeReader
import com.pharmeasy.barcode.Event
import com.pharmeasy.barcode.ModeSelectedListener
import com.pharmeasy.barcode.ScannerType
import javax.inject.Inject

class SortActivity : ScannerBaseActivity(), SortationView, View.OnClickListener,
    BarcodeScannerInterface
    {

    @Inject
    lateinit var presenter: SortPresenter

    private var backAllowed = true

    private lateinit var binding: ActivityScanSortBinBinding

    private var modeSelected = false

    private var barcodeScanned = false

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
                    //barcodeView?.setTorchListener(this@SortActivity)
                }
            }
        }
    }

    val scannedOrderList = FixedSizeList<ScannedOrder>(4)

    private lateinit var adapter: ScannedOrdersAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanSortBinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initScanner()
        initSortViews()
        initDagger()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermissions()
        }

    }

    private fun initDagger() {
        DaggerActivityComponents.builder()
            .networkComponent((application as SortationApplication).getNetworkComponent())
            .build().inject(this@SortActivity)

        presenter.onAttachView(this, this)
    }


    private fun initSortViews() {

        val toolbar = findViewById<View>(R.id.toolBar1) as Toolbar
        setSupportActionBar(toolbar)

        binding.toolBar1.toolbarTitle.text = getString(R.string.sortation)

        backAllowed = true

        val actionbar: ActionBar? = supportActionBar
        actionbar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_back_button_circular)
            setDisplayShowTitleEnabled(false)
        }

        binding.scanContent.scanContent.scanLabel.text = getString(R.string.scan_shipment)
        if (PreferenceHelper.singleScanSortation)
            setupListView()
        else
            binding.scanContent.scanDeliveryLabel.text = getString(R.string.scan_shipment)
        binding.scanContent.binName.visibility = View.GONE
        binding.scanContent.closeBag.visibility = View.VISIBLE
        binding.scanContent.tvMessage.visibility = View.GONE

        binding.orderDetail.orderDetail.visibility = View.GONE

        binding.scanContent.closeBag.setOnClickListener(this)

        if (!com.goflash.dispatch.util.hasFlash(applicationContext)) {
            binding.scanContent.scanContentCamera.imgFlashlightOn.visibility = View.GONE
        }

        binding.scanContent.scanContentCamera.imgFlashlightOn.setOnClickListener(this)
        binding.scanContent.scanContentCamera.imgFlashlightOff.setOnClickListener(this)
        binding.scanContent.scanContent.imgScanner.setOnClickListener(this)
        binding.scanContent.scanContentCamera.imgScannerCamera.setOnClickListener(this)
    }

    private fun setupListView(){
        adapter = ScannedOrdersAdapter(this@SortActivity, scannedOrderList)
        binding.scanContent.rvScannedOrders.layoutManager = LinearLayoutManager(this)
        binding.scanContent.rvScannedOrders.adapter = adapter

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
                    binding.scanContent
                        .scanContentCamera.imgFlashlightOn.setImageResource(
                        R.drawable.ic_flash_on_black_24dp
                    )
                }
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


    override fun onPause() {

        if (!modeSelected)
            SortationApplication.getSortationApplicationClass().getBarcodeReader().clearMode()

        super.onPause()
    }


    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        if (intent?.getBooleanExtra(cancelled, false) == true)
            presenter.reInitialize()
    }

    override fun showBinName(binNumber: String?, orderId: String?) {
        hideProgress()

        barcodeScanned = false

        backAllowed = false

        binding.scanContent.binName.text = binNumber
        binding.orderDetail.orderId.text = orderId
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


    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.close_bag -> startScanSortedBinActivity()
            R.id.img_flashlight_on -> {
                (application as SortationApplication).getBarcodeReader().toggleTorch()
            }

            R.id.img_scanner -> {
                SortationApplication.getSortationApplicationClass().getBarcodeReader()
                    .selectScanner(this, intent, R.id.zxing_scanner_view, binding.scanContent.etScan, listener)
            }
            R.id.img_scanner_camera -> {
                SortationApplication.getSortationApplicationClass().getBarcodeReader()
                    .selectScanner(this, intent, R.id.zxing_scanner_view, binding.scanContent.etScan, listener)
            }
        }
    }


    override fun onBarcodeScanned(barcode: String) {
        showProgress()
        if(!barcodeScanned) {
            barcodeScanned = true
            presenter.onBinScan(barcode)
        }
    }

    override fun onSuccessCancelledOrderScan(result: PackageDto) {
        hideProgress()
        barcodeScanned = false
        val intent = Intent(this, CancelledActivity::class.java)
        intent.putExtra(scannedPackage, result)

        startActivity(intent)
    }

    override fun onFailure(error: Throwable?) {
        hideProgress()
        processError(error)
        barcodeScanned = false
    }

    override fun displayScannedOrders(order: ScannedOrder) {
        hideProgress()
        scannedOrderList.add(order)
        showLastScanned(order)
        adapter.notifyDataSetChanged()
        barcodeScanned = false

    }

    private fun showLastScanned(order: ScannedOrder){
        binding.scanContent.svScannedOrders.visibility = View.VISIBLE
        binding.scanContent.tvBinNumber.text = order.binNumber
        binding.scanContent.tvPackageId.text = order.packageId
        binding.scanContent.tvLbn.text = order.lbn
        binding.scanContent.tvRefId.text = order.referenceId

        //startTimer()
    }

    override fun onSuccessBinScan() {
        hideProgress()
        Toaster.show(this, getString(R.string.task_completed))
        initSortViews()
        barcodeScanned = false
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDetachView()
    }

    private fun startScanSortedBinActivity() {
        val intent = Intent(this@SortActivity, ScanSortedBinActivity::class.java)
        intent.putExtra(close_Bag, true)
        startActivity(intent)
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

    override fun onBackPressed() {
        if (!backAllowed)
            showToast(this, getString(R.string.cannot_go_back))
        else
            super.onBackPressed()

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

    private fun startTimer() {

        val countDownTimer = object : CountDownTimer((30 * 1000).toLong(), 100) {

            override fun onTick(millisUntilFinished: Long) {

            }

            override fun onFinish() {
                binding.scanContent.cvScannedOrders.visibility = View.GONE
            }
        }

        countDownTimer.start()
    }

}