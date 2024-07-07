package com.goflash.dispatch.features.audit.ui

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.text.InputType
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import androidx.annotation.RequiresApi
import androidx.camera.core.TorchState
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.goflash.dispatch.R
import com.goflash.dispatch.SortationApplication
import com.goflash.dispatch.app_constants.CAMERA_REQ
import com.goflash.dispatch.databinding.ActivityAuditScanBinding
import com.goflash.dispatch.di.components.DaggerActivityComponents
import com.goflash.dispatch.features.audit.presenter.AuditScanPresenter
import com.goflash.dispatch.features.audit.view.AuditScanView
import com.goflash.dispatch.listeners.BarcodeScannerInterface
import com.goflash.dispatch.ui.activity.HomeActivity
import com.goflash.dispatch.ui.activity.ScannerBaseActivity
import com.goflash.dispatch.util.hasFlash
import com.pharmeasy.barcode.BarcodeReader
import com.pharmeasy.barcode.Event
import com.pharmeasy.barcode.ModeSelectedListener
import com.pharmeasy.barcode.ScannerType
import javax.inject.Inject


class AuditScanActivity : ScannerBaseActivity(), View.OnClickListener,
    AuditScanView, BarcodeScannerInterface {

    @Inject
    lateinit var mPresenter: AuditScanPresenter

    private var lastText: String = ""

    private var modeSelected = false

    private var max_time = 2

    private lateinit var binding: ActivityAuditScanBinding

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
                    //barcodeView?.setTorchListener(this@AuditScanActivity)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuditScanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initScanner()
        initViews()
        initDagger()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermissions()
        }
    }

    private fun initDagger() {
        DaggerActivityComponents.builder()
            .networkComponent((application as SortationApplication).getNetworkComponent())
            .build().inject(this@AuditScanActivity)

        mPresenter.onAttachView(this, this@AuditScanActivity)

        mPresenter.sendIntent(intent)
    }

    private fun initViews() {

        binding.toolBar1.toolbarTitle.text = getString(R.string.scan_bag)

       /* item_label.text = SessionService.assignedAssetName
        order_id.text = SessionService.name*/

        if (!hasFlash(applicationContext)) {
            binding.scanContentLayout.scanContentCamera.imgFlashlightOn.visibility = View.GONE
        }

        binding.toolBar1.iVProfileHome.visibility = View.GONE
        binding.btnDone.setOnClickListener(this)
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

            R.id.btnDone -> {
                showALertDialog()
            }
            R.id.img_flashlight_on -> {
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

    override fun onBarcodeScanned(barcode: String) {
        if (barcode == lastText || barcode.isEmpty())
            return
        lastText = barcode
        mPresenter.onBarcodeScanned(barcode)
        binding.scannedBarcode.scannedBarcode.visibility = View.VISIBLE
        binding.scannedBarcode.tvBarcode.text = barcode
        startTextAnimation()
    }

    private fun startTextAnimation(){

        // Start from 0.1f if you desire 90% fade animation
        // Start from 0.1f if you desire 90% fade animation
        val fadeIn: Animation = AlphaAnimation(0.0f, 1.0f)
        fadeIn.duration = 2000
        fadeIn.startOffset = 200
        // End to 0.1f if you desire 90% fade animation
        // End to 0.1f if you desire 90% fade animation
        val fadeOut: Animation = AlphaAnimation(1.0f, 0.0f)
        fadeOut.duration = 1000
        fadeOut.startOffset = 500

        fadeIn.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationEnd(arg0: Animation) {
               startTimer()
            }

            override fun onAnimationRepeat(arg0: Animation) {}
            override fun onAnimationStart(arg0: Animation) {}
        })

        binding.scannedBarcode.tvBarcode.startAnimation(fadeIn)

    }


    private fun startTimer() {

        val countDownTimer = object : CountDownTimer((max_time * 1000).toLong(), 100) {
            override fun onTick(millisUntilFinished: Long) {
                binding.scannedBarcode.scannedBarcode.visibility = View.VISIBLE
            }

            override fun onFinish() {
                binding.scannedBarcode.scannedBarcode.visibility = View.GONE
            }
        }

        countDownTimer.start()
    }

    override fun onSuccess() {
        hideProgress()
        val intent = Intent(this@AuditScanActivity, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
    }

    override fun onFailure(error: Throwable?) {
        hideProgress()
        processError(error)
    }

    override fun showAuditInactive() {
        hideProgress()
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.audit_inactive_title))
        builder.setMessage(getString(R.string.audit_inactive_message))
        builder.setPositiveButton(getString(R.string.ok)) { dialog, which ->
            val intent = Intent(this, AuditActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }

        builder.show()
    }

    override fun showCount(bagCount: Long, shipmentCount: Long) {
        binding.scanCount.tvBagCount.text = "$bagCount"
        binding.scanCount.tvShipmentCount.text = "$shipmentCount"

        binding.btnDone.isEnabled = true
        binding.btnDone.setBackgroundResource(R.color.md_orange_800)

    }

    private fun showALertDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.audit_complete_title))
        builder.setMessage(getString(R.string.audit_complete_message))
        builder.setPositiveButton(getString(R.string.ok)) { dialog, which ->
            showProgress()
            mPresenter.completeAudit()
        }
        builder.setNegativeButton(getString(R.string.cancel), null)
        // builder.setOnCancelListener { dialog -> finish() }
        builder.show()
    }

    override fun onBackPressed() {
        showToast(this, getString(R.string.cannot_go_back))
    }

    override fun onDestroy() {
        super.onDestroy()
        mPresenter.onDetachView()
    }


}
