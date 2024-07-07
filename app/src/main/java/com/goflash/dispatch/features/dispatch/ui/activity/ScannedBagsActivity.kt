package com.goflash.dispatch.features.dispatch.ui.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
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
import com.goflash.dispatch.R
import com.goflash.dispatch.SortationApplication
import com.goflash.dispatch.app_constants.CAMERA_REQ
import com.goflash.dispatch.app_constants.bagUpdated
import com.goflash.dispatch.databinding.ActivityScannedShipmentsBinding
import com.goflash.dispatch.di.components.DaggerActivityComponents
import com.goflash.dispatch.features.bagging.ui.adapter.ScannedBagsAdapter
import com.goflash.dispatch.features.dispatch.presenter.ScannedBagsPresenter
import com.goflash.dispatch.features.dispatch.view.ScannedBagsView
import com.goflash.dispatch.listeners.BarcodeScannerInterface
import com.goflash.dispatch.ui.activity.ScannerBaseActivity
import com.goflash.dispatch.util.hasFlash
import com.google.android.material.snackbar.Snackbar
import com.pharmeasy.barcode.BarcodeReader
import com.pharmeasy.barcode.Event
import com.pharmeasy.barcode.ModeSelectedListener
import com.pharmeasy.barcode.ScannerType
import javax.inject.Inject

class ScannedBagsActivity : ScannerBaseActivity(), ScannedBagsView, View.OnClickListener,
    BarcodeScannerInterface {

    @Inject
    lateinit var scannedBagsPresenter: ScannedBagsPresenter

    private lateinit var binding: ActivityScannedShipmentsBinding

    private var modeSelected = false
    private var addBag: Boolean? = false

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
                    //barcodeView?.setTorchListener(this@ScannedBagsActivity)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScannedShipmentsBinding.inflate(layoutInflater)

        setContentView(binding.root)

        initScanner()
        initDagger()
        initViews()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermissions()
        }
    }

    fun initDagger() {
        DaggerActivityComponents.builder().networkComponent((application as SortationApplication).getNetworkComponent())
            .build().inject(this@ScannedBagsActivity)

        scannedBagsPresenter.onAttachView(this, this)

    }

    fun initViews() {
        binding.orderDetail.orderDetail.visibility = View.GONE
        addBag = intent.getBooleanExtra("addBag", false)

        val toolbar = findViewById<View>(R.id.toolBar1) as Toolbar
        setSupportActionBar(toolbar)

        val actionbar: ActionBar? = supportActionBar
        actionbar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_back_button_circular)
            setDisplayShowTitleEnabled(false)
        }

        binding.tvMessage.text = getString(R.string.scan_to_remove_bags)
        binding.toolBar1.toolbarTitle.text = getString(R.string.scanned_bags)

        binding.shipmentList.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        binding.shipmentList.adapter = ScannedBagsAdapter(this, scannedBagsPresenter)

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

    override fun onClick(v: View?) {

        when (v?.id) {

            R.id.img_flashlight_on ->{
                (application as SortationApplication).getBarcodeReader().toggleTorch()
            }

            R.id.img_scanner ->{
                SortationApplication.getSortationApplicationClass().getBarcodeReader().selectScanner(this, intent, R.id.zxing_scanner_view,binding.scanContentLayout.etScan, listener)
            }
            R.id.img_scanner_camera -> {
                SortationApplication.getSortationApplicationClass().getBarcodeReader().selectScanner(this, intent, R.id.zxing_scanner_view, binding.scanContentLayout.etScan, listener)
            }

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scannedBagsPresenter.onDeatchView()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                scannedBagsPresenter.getUpdatedBagList(false)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBarcodeScanned(barcode: String) {
        scannedBagsPresenter.onBarcodeScanned(barcode)
    }

    override fun onFailure(error: Throwable?) {
        hideProgress()
        processError(error)
    }

    override fun refreshList() {
        binding.shipmentList.adapter?.notifyDataSetChanged()
    }

    override fun setCount(count: Int) {
        binding.chemistName.text = String.format(getString(R.string.bags_label),count)
    }

    override fun showSnackBar(message: String) {
        val snack = Snackbar.make(binding.shipmentList,message, Snackbar.LENGTH_LONG)
            .setAction("Undo") {
                scannedBagsPresenter.undoRemove()
            }

        snack.show()
    }

    override fun finishActivity() {
        val intent = Intent(this@ScannedBagsActivity, DispatchBagActivity::class.java)
        intent.putExtra(bagUpdated,true)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
    }

    override fun onBackPressed() {
        scannedBagsPresenter.getUpdatedBagList(addBag!!)
    }

}