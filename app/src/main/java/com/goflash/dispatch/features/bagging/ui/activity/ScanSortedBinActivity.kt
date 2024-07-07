package com.goflash.dispatch.features.bagging.ui.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.Toolbar
import android.view.MenuItem
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.pharmeasy.barcode.BarcodeReader
import com.pharmeasy.barcode.Event
import com.goflash.dispatch.R
import com.goflash.dispatch.SortationApplication
import com.goflash.dispatch.app_constants.CAMERA_REQ
import com.goflash.dispatch.app_constants.scannedPackageList
import com.goflash.dispatch.data.PackageDto
import com.goflash.dispatch.di.components.DaggerActivityComponents
import com.goflash.dispatch.features.bagging.presenter.ScanSotrBinPresenter
import com.goflash.dispatch.features.bagging.view.ScanSortBinView
import com.goflash.dispatch.ui.activity.ScannerBaseActivity
import com.goflash.dispatch.util.hasFlash
import com.pharmeasy.barcode.ModeSelectedListener
import android.text.InputType
import androidx.camera.core.TorchState
import com.goflash.dispatch.databinding.ActivityScanSortBinBinding
import com.goflash.dispatch.listeners.BarcodeScannerInterface
import com.pharmeasy.barcode.ScannerType
import javax.inject.Inject

class ScanSortedBinActivity : ScannerBaseActivity(), ScanSortBinView, View.OnClickListener,
    BarcodeScannerInterface {

    @Inject
    lateinit var presenter: ScanSotrBinPresenter

    private var closeBag = false

    private lateinit var binding: ActivityScanSortBinBinding

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
                    binding.scanContent.etScan.requestFocus()
                }
                ScannerType.CAMERA_SCANNER.displayName -> {
                    binding.scanContent.scanContent.scanContent.visibility = View.GONE
                    binding.scanContent.scanContentCamera.scanTask.visibility = View.VISIBLE
                    binding.scanContent.etScan.visibility = View.GONE
                   // barcodeView?.setTorchListener(this@ScanSortedBinActivity)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanSortBinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initScanner()
        initDagger()
        initViews()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermissions()
        }
    }

    private fun initDagger() {
        DaggerActivityComponents.builder().networkComponent((application as SortationApplication).getNetworkComponent())
                .build().inject(this@ScanSortedBinActivity)

        presenter.onAttachView(this@ScanSortedBinActivity, this)
    }

    private fun initViews() {

       // barcodeView?.setTorchListener(this)

        presenter.sendIntent(intent)

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
        binding.scanContentLayout.scanContentCamera.imgFlashlightOn.visibility = View.GONE
        binding.scanContentLayout.scanContentCamera.imgFlashlightOff.visibility = View.VISIBLE

    }

    override fun onTorchOff() {
        binding.scanContentLayout.scanContentCamera.imgFlashlightOff.visibility = View.GONE
        binding.scanContentLayout.scanContentCamera.imgFlashlightOn.visibility = View.VISIBLE
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

    override fun onClick(v: View?) {

        when (v?.id) {

            R.id.img_flashlight_on ->{
                (application as SortationApplication).getBarcodeReader().toggleTorch()
            }

            R.id.img_flashlight_off ->{
                //barcodeView?.setTorchOff()
            }
            R.id.img_scanner ->{
                SortationApplication.getSortationApplicationClass().getBarcodeReader().selectScanner(this, intent, R.id.zxing_scanner_view,binding.scanContent.etScan, listener)
            }
            R.id.img_scanner_camera -> {
                SortationApplication.getSortationApplicationClass().getBarcodeReader().selectScanner(this, intent, R.id.zxing_scanner_view, binding.scanContent.etScan, listener)
            }

        }
    }

    override fun showBinName(binNumber: String?, orderId: String?) {
        binding.scanContent.binName.text = binNumber
        binding.orderDetail.orderId.text = orderId
        binding.scanContent.tvMessage.visibility = View.VISIBLE

        val toolbar = findViewById<View>(R.id.toolBar1) as Toolbar
        setSupportActionBar(toolbar)

        val actionbar: ActionBar? = supportActionBar
        actionbar?.apply {
            setDisplayHomeAsUpEnabled(false)
            setDisplayShowTitleEnabled(false)
        }

        binding.toolBar1.toolbarTitle.text = getString(R.string.scan_bin)
        binding.scanContent.scanDeliveryLabel.text = getString(R.string.scan_bin_number)
        binding.scanContent.scanContent.scanLabel.text = getString(R.string.scan_bin)
    }

    override fun hideOrderLayout() {
        binding.orderDetail.orderDetail.visibility = View.GONE
        binding.scanContent.scanDeliveryLabel.text = getString(R.string.scan_bin)
        binding.scanContent.scanContent.scanLabel.text = getString(R.string.scan_bin)

        val toolbar = findViewById<View>(R.id.toolBar1) as Toolbar
        setSupportActionBar(toolbar)

        val actionbar: ActionBar? = supportActionBar
        actionbar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_back_button_circular)
            setDisplayShowTitleEnabled(false)
        }

        binding.toolBar1.toolbarTitle.text = getString(R.string.close_bag)

        closeBag = true

    }

    override fun onBarcodeScanned(barcode: String) {
        showProgress()

        presenter.onBarcodeScanned(barcode)
        /* if (barcode.equals(scannedOrder?.binNumber)) {
             showProgress()
             presenter.onBinScan(scannedPackage!!)
         } else
             showToast(this, getString(R.string.invalid_bin))*/

    }

    override fun takeToBagDetailScreen(packageDto: PackageDto) {
        hideProgress()
        val intent = Intent(this@ScanSortedBinActivity, BagDetailActivity::class.java)
        intent.putExtra(scannedPackageList, packageDto)
        startActivity(intent)
    }

    override fun onSuccess() {
        hideProgress()
        showToast(this, getString(R.string.task_completed))
        finish()

    }

    override fun onFailure(error: Throwable?) {
        hideProgress()
        processError(error)

    }

    override fun onBackPressed() {
        if (!closeBag)
            showToast(this, getString(R.string.cannot_go_back))
        else
            super.onBackPressed()
    }



    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            android.R.id.home -> {

                finish()
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDetachView()
    }

}