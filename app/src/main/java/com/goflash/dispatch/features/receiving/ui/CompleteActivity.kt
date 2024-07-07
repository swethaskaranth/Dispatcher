package com.goflash.dispatch.features.receiving.ui

import android.Manifest
import android.app.AlertDialog
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
import co.uk.rushorm.core.RushCore
import com.goflash.dispatch.R
import com.goflash.dispatch.SortationApplication
import com.goflash.dispatch.app_constants.CAMERA_REQ
import com.goflash.dispatch.app_constants.show_completed
import com.goflash.dispatch.databinding.ActivityCompleteBinding
import com.goflash.dispatch.di.components.DaggerActivityComponents
import com.goflash.dispatch.features.receiving.presenter.CompletePresenter
import com.goflash.dispatch.features.receiving.view.CompleteScanView
import com.goflash.dispatch.listeners.BarcodeScannerInterface
import com.goflash.dispatch.ui.activity.HomeActivity
import com.goflash.dispatch.ui.activity.ScannerBaseActivity
import com.goflash.dispatch.util.hasFlash
import com.pharmeasy.barcode.BarcodeReader
import com.pharmeasy.barcode.Event
import com.pharmeasy.barcode.ModeSelectedListener
import com.pharmeasy.barcode.ScannerType
import java.util.regex.Pattern
import javax.inject.Inject

class CompleteActivity : ScannerBaseActivity(), CompleteScanView, View.OnClickListener,
    BarcodeScannerInterface {

    @Inject
    lateinit var mPresenter: CompletePresenter

    private var barcodeScanned = false

    private lateinit var binding: ActivityCompleteBinding

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
                   // barcodeView?.setTorchListener(this@CompleteActivity)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCompleteBinding.inflate(layoutInflater)
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
            .build().inject(this@CompleteActivity)

        mPresenter.onAttachView(this, this)
        mPresenter.onIntent(intent)
    }

    override fun onResume() {
        super.onResume()

        mPresenter.onTaskResume()

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


    private fun initViews(){
        binding.toolBar1.toolbarTitle.text = if(mPresenter.getSealrequired()) getString(R.string.scan_vehicle) else getString(R.string.complete)
        binding.scanContentLayout.scanContent.scanLabel.text = getString(R.string.scan_seal_barcode)

        binding.scanContentLayout.scanContent.barcodeLayout.imageView.setImageResource(R.drawable.ic_vehicle_barcode)

        val toolbar = findViewById<View>(R.id.toolBar1) as Toolbar
        setSupportActionBar(toolbar)

        val actionbar: ActionBar? = supportActionBar
        actionbar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_back_button_circular)
            setDisplayShowTitleEnabled(false)
        }

        if (!hasFlash(applicationContext)) {
            binding.scanContentLayout.scanContentCamera.imgFlashlightOn.visibility = View.GONE
        }

        binding.scanContentLayout.scanContentCamera.imgFlashlightOn.setOnClickListener(this)
        binding.scanContentLayout.scanContentCamera.imgFlashlightOff.setOnClickListener(this)

        binding.btnProceed.setOnClickListener { mPresenter.verifyVehicleSeal() }
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

    override fun onBarcodeScanned(barcode: String) {
        val pattern1 = Pattern.compile("[^A-Za-z0-9]")

        if (pattern1.matcher(barcode).find() || barcode.isEmpty()) {
            onFailure(Throwable("Scanned barcode $barcode contains special characters. Please scan again."))
            return
        }


        shouldEnableScanner(false)
        disableScanner()
        if (SortationApplication.getSortationApplicationClass()
                .getBarcodeReader().UIView!!
        ) {
            showProgress()
            mPresenter.onBagScanned(barcode)
        }else
            showALertDialog(barcode)

        //  }

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

    private fun showHomeActivity(){
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finishAffinity()
    }

    override fun onFailure(error: Throwable?) {
        barcodeScanned = false
        hideProgress()
        if(error != null)
            processError(error)
    }

    override fun onSuccess() {
        hideProgress()

        barcodeScanned = false

        showToast(this, getString(R.string.task_completed))
        showHomeActivity()
    }

    override fun onShowProgress() {
        showProgress()
    }

    override fun onHideProgress() {
        hideProgress()
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

    override fun onDestroy() {
        super.onDestroy()
        mPresenter.onDetachView()
    }

    override fun disableScanner(tripid : String, sprinter : String, time : String) {
        binding.scanContentLayout.scanContentLayout.visibility = View.GONE
       binding.tripDetail.layoutOptionalVehicleSeal.visibility = View.VISIBLE

        binding.tripDetail.tvLabel.text = getString(R.string.trip_id)
        binding.tripDetail.tvTripId.text = tripid
        binding.tripDetail.tvDriver.text = "${sprinter},"
        binding.tripDetail.tvTime.text = time

        binding.toolBar1.toolbarTitle.text = getString(R.string.complete)

        binding.btnProceed.text = getString(R.string.complete_task)
        binding.btnProceed.visibility = View.VISIBLE
        binding.btnProceed.isEnabled = true
        binding.btnProceed.setBackgroundColor(ContextCompat.getColor(this, R.color.md_orange_800))
    }

    private fun showALertDialog(barcode: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.create_trip_title))
        builder.setMessage(String.format(getString(R.string.create_trip_message), barcode))
        builder.setPositiveButton(getString(R.string.ok)) { i1, i2 ->
            showProgress()
            mPresenter.onBagScanned(barcode)
        }
        builder.setNegativeButton(getString(R.string.cancel)) { i1, i2 ->
            // barcodeScanned = false
            enableScanner()
        }
        builder.show()
    }

    override fun showErrorAndRedirect(message: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.trip_completed))
        builder.setMessage(message)
        builder.setPositiveButton(getString(R.string.ok)) { i1, i2 ->
            val intent = Intent(this@CompleteActivity,ReceivingActivity::class.java)
            intent.putExtra(show_completed, false)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }
        builder.show()
    }
}
