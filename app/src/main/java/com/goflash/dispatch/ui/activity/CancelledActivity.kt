package com.goflash.dispatch.ui.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.Toolbar
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
import com.goflash.dispatch.app_constants.cancelled
import com.goflash.dispatch.di.components.DaggerActivityComponents
import com.goflash.dispatch.features.bagging.ui.activity.SortActivity
import com.goflash.dispatch.features.dispatch.ui.activity.ScanDispatchBinActivity
import com.goflash.dispatch.presenter.CancelledPresenter
import com.goflash.dispatch.presenter.views.CancelledView
import com.goflash.dispatch.ui.adapter.CancelledAdapter
import com.goflash.dispatch.util.hasFlash
import com.pharmeasy.barcode.ModeSelectedListener
import android.text.InputType
import androidx.camera.core.TorchState
import com.goflash.dispatch.databinding.ActivityScanCancelledBinBinding
import com.goflash.dispatch.listeners.BarcodeScannerInterface
import com.pharmeasy.barcode.ScannerType
import javax.inject.Inject

class CancelledActivity : ScannerBaseActivity(), CancelledView, View.OnClickListener,
    BarcodeScannerInterface {

    @Inject
    lateinit var presenter: CancelledPresenter

    private var toolbar: Toolbar? = null

    private lateinit var binding: ActivityScanCancelledBinBinding

    private var modeSelected = false

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
                   // barcodeView?.setTorchListener(this@CancelledActivity)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanCancelledBinBinding.inflate(layoutInflater)
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
            .build().inject(this@CancelledActivity)

        presenter.onAttachView(this@CancelledActivity, this)

    }

    private fun initViews() {

        toolbar = findViewById<View>(R.id.toolBar1) as Toolbar
        setSupportActionBar(toolbar)

        binding.toolBar1.toolbarTitle.text = getString(R.string.scan_delivery_label)

        val actionbar: ActionBar? = supportActionBar
        actionbar?.apply {
            setDisplayHomeAsUpEnabled(false)
            setDisplayShowTitleEnabled(false)
        }

        presenter.sendIntent(intent)

        binding.cancelledList.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(
            this@CancelledActivity,
            androidx.recyclerview.widget.LinearLayoutManager.VERTICAL,
            false
        )
        binding.cancelledList.adapter = CancelledAdapter(this, presenter)

        if (!hasFlash(applicationContext)) {
            binding.scanContentLayout.scanContentCamera.imgFlashlightOn.visibility = View.GONE
        }

        binding.scanContentLayout.scanContentCamera.imgFlashlightOn.setOnClickListener(this)
        binding.scanContentLayout.scanContentCamera.imgFlashlightOff.setOnClickListener(this)
        binding.scanContentLayout.scanContent.imgScanner.setOnClickListener(this)
        binding.scanContentLayout.scanContentCamera.imgScannerCamera.setOnClickListener(this)

    }

    override fun initScanner() {
        initBarcodeScanner(this)
        if (!SortationApplication.getSortationApplicationClass().getBarcodeReader().UIView!!) {

            SortationApplication.getSortationApplicationClass().getBarcodeReader()
                .initializeScanner(this, intent, R.id.zxing_scanner_view,binding.scanContentLayout.etScan,listener)
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

        }else{
            binding.scanContentLayout.scanContent.imgScanner.visibility = View.GONE
        }

        binding.scanContentLayout.etScan.inputType = InputType.TYPE_NULL


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

    override fun onBarcodeScanned(barcode: String) {
        showProgress()
        presenter.onBarcodeScanned(barcode)
    }

    override fun onClick(v: View?) {

        when (v?.id) {

            R.id.img_flashlight_on ->{
                (application as SortationApplication).getBarcodeReader().toggleTorch()
            }
            R.id.img_scanner ->{
                SortationApplication.getSortationApplicationClass().getBarcodeReader().selectScanner(this, intent, R.id.zxing_scanner_view, binding.scanContentLayout.etScan, listener)
            }
            R.id.img_scanner_camera -> {
                SortationApplication.getSortationApplicationClass().getBarcodeReader().selectScanner(this, intent, R.id.zxing_scanner_view, binding.scanContentLayout.etScan, listener)
            }

        }
    }

    override fun refreshList() {
        hideProgress()
        binding.cancelledList.adapter?.notifyDataSetChanged()
    }

    override fun showScanBin(binNumber: String?) {
        binding.orderCancelledLabel.visibility = View.GONE
        binding.cancelledList.visibility = View.GONE

        binding.toolBar1.toolbarTitle.text = getString(R.string.scan_bin)

        binding.scanContentLayout.scanContent.scanLabel.text = getString(R.string.scan_bin)
        binding.scanDeliveryLabel.text = getString(R.string.scan_bin_number)
        binding.scanDeliveryLabel.visibility = View.VISIBLE
        binding.binName.visibility = View.VISIBLE
        binding.binName.text = binNumber
        binding.cancelledList.visibility = View.GONE
    }

   /* override fun onData(scanDataCollection: ScanDataCollection?) {
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

    override fun finishSortationTask() {
        hideProgress()
        showToast(this, getString(R.string.task_completed))

        val intent = Intent(this, SortActivity::class.java)
        intent.putExtra(cancelled,true)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
    }

    override fun takeToMainActivity() {
        hideProgress()
        //showToast(this, getString(R.string.task_completed))

        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
    }

    override fun takeToScanActivity() {
        hideProgress()
        val intent = Intent(this, ScanDispatchBinActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }

    override fun showOrHideScanLabel(singleItem: Boolean, status: String?, binNumber: String?) {
        if (singleItem) {

            binding.toolBar1.toolbarTitle.text = getString(R.string.scan_bin)

            binding.scanContentLayout.scanContent.scanLabel.text = getString(R.string.scan_bin)
            binding.scanDeliveryLabel.text = getString(R.string.scan_bin_number)
            binding.scanDeliveryLabel.visibility = View.VISIBLE
            binding.binName.visibility = View.VISIBLE
            binding.binName.text = binNumber

        }

        binding.orderCancelledLabel.text = String.format(getString(R.string.nondispatchable_order), status)//"${status} Order"
    }

    override fun onFailure(error: Throwable?) {
        hideProgress()
        processError(error)

    }

    override fun onBackPressed() {
        showToast(this@CancelledActivity, getString(R.string.cannot_go_back))
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDetachView()
    }

}