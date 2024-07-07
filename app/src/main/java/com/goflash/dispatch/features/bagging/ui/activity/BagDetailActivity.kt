package com.goflash.dispatch.features.bagging.ui.activity

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
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
import com.goflash.dispatch.app_constants.close_Bag
import com.goflash.dispatch.app_constants.scannedPackageList
import com.goflash.dispatch.data.PackageDto
import com.goflash.dispatch.databinding.ActivityBagDetailBinding
import com.goflash.dispatch.di.components.DaggerActivityComponents
import com.goflash.dispatch.features.bagging.presenter.BagDetailPresenter
import com.goflash.dispatch.features.bagging.view.BagDetailView
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

class BagDetailActivity : ScannerBaseActivity(), BagDetailView, View.OnClickListener,
    BarcodeScannerInterface {

    @Inject
    lateinit var bagDetailPresenter: BagDetailPresenter

    private var bag_created = false

    private var barcodeScanned = false

    lateinit var binding: ActivityBagDetailBinding
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
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBagDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initScanner()
        initDagger()
        initViews()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermissions()
        }
    }

    fun initViews() {
        binding.chemistName.text = getString(R.string.scanned_shipments)
        binding.scanContentLayout.scanContent.scanLabel.text = getString(R.string.scan_seal_barcode)

        binding.proceedBtn.text = getString(R.string.complete_task)
        if (!bag_created) {
            binding.proceedBtn.isEnabled = false
            binding.proceedBtn.setBackgroundResource(R.drawable.disable_button)
        }

        binding.proceedBtn.setOnClickListener(this)
        binding.scanCount.tvViewDetails.setOnClickListener(this)

        val toolbar = findViewById<View>(R.id.toolBar1) as Toolbar
        setSupportActionBar(toolbar)

        val actionbar: ActionBar? = supportActionBar
        actionbar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_back_button_circular)
            setDisplayShowTitleEnabled(false)
        }

        binding.toolBar1.toolbarTitle.text = getString(R.string.close_bag)

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
        } else {
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

    override fun onResume() {
        super.onResume()
        bagDetailPresenter.getShipmentCount()
    }

    override fun onPause() {


        if (!modeSelected)
            SortationApplication.getSortationApplicationClass().getBarcodeReader().clearMode()

        super.onPause()
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


    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let { bagDetailPresenter.sendIntent(intent) }

    }


    fun initDagger() {
        DaggerActivityComponents.builder()
            .networkComponent((application as SortationApplication).getNetworkComponent())
            .build().inject(this@BagDetailActivity)

        bagDetailPresenter.onAttachView(this@BagDetailActivity, this)
        bagDetailPresenter.sendIntent(intent)
    }

    override fun onBarcodeScanned(barcode: String) {
        val pattern1 = Pattern.compile("[^A-Za-z0-9]")

        if (pattern1.matcher(barcode).find() || barcode.isEmpty()) {
            onFailure(Throwable("Scanned barcode $barcode contains special characters. Please scan again."))
            return
        }

        shouldEnableScanner(false)
        disableScanner()
        /*  if (!bag_created) {
              if (!barcodeScanned) {
                  barcodeScanned = true*/
        if (SortationApplication.getSortationApplicationClass()
                .getBarcodeReader().UIView!!
        ) {
            showProgress()
            bagDetailPresenter.onBarcodeScanned(barcode)
        } else
            showAlertDialog(getString(R.string.close_bag), String.format(getString(R.string.create_bag_message), barcode), {
                v1, v2 ->
                showProgress()
                bagDetailPresenter.onBarcodeScanned(barcode)
            }, {
                v1, v2 -> enableScanner()
            })

        /* }

     } else
         processError(Throwable("Bag Already Created"))*/
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.proceed_btn -> {
                if (bagDetailPresenter.shouldShowAlert())
                    showAlertDialog(
                    getString(
                        R.string.close_bin
                    ), getString(R.string.close_bin_message), { _, _ ->
                        showProgress()
                        bagDetailPresenter.onProceedClicked()
                    }, { _, _ ->
                    }) else
                        goToHomeActivity()
            }
            R.id.tv_view_details -> bagDetailPresenter.getShipmentList()
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {

                if (!bag_created) {
                    finish()
                    true
                } else {
                    showToast(this@BagDetailActivity, getString(R.string.cannot_go_back))
                    true
                }
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun goToHomeActivity() {
        hideProgress()
        val intent = Intent(this@BagDetailActivity, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
    }

    override fun goToShipmentsActivity(packageDto: PackageDto) {
        val intent = Intent(this@BagDetailActivity, ScannedShipmentsActivity::class.java)
        intent.putExtra(scannedPackageList, packageDto)
        intent.putExtra(close_Bag, bag_created)
        startActivity(intent)
    }

    override fun showShipmentCount(size: Int?) {
        binding.scanCount.itemCount.text = "$size"
    }


    override fun onFailure(error: Throwable?) {
        barcodeScanned = false
        hideProgress()
        processError(error)
    }

    override fun restoreBagState() {
        bag_created = true

        binding.proceedBtn.isEnabled = true
        binding.proceedBtn.setBackgroundColor(ContextCompat.getColor(this, R.color.md_orange_800))
    }

    override fun setBagDestination(destination: String) {
        binding.orderDetail.orderDetail.visibility = View.VISIBLE
        binding.orderDetail.itemLabel.text = String.format(getString(R.string.bag_destination), destination)
    }

    override fun onSuccess(message: String) {
        hideProgress()
        showToast(this@BagDetailActivity, message)

        barcodeScanned = false
        binding.proceedBtn.text = getString(R.string.complete_task)
        restoreBagState()

    }

    override fun onDestroy() {
        super.onDestroy()
        bagDetailPresenter.onDetachView()
    }

    override fun onBackPressed() {
        if (!bag_created)
            super.onBackPressed()
        else
            showToast(this@BagDetailActivity, getString(R.string.cannot_go_back))
    }

    private fun showAlertDialog(title: String, message: String, positiveButtonCallback: (var1: DialogInterface, var2: Int) -> Unit, negativeButtonCallback: (var1: DialogInterface, var2: Int) -> Unit) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton(getString(R.string.ok), positiveButtonCallback)
        builder.setNegativeButton(getString(R.string.cancel), negativeButtonCallback)
        builder.show()
    }

    override fun setTripDetails(binNumber: String) {
        binding.scanContentLayout.scanContentLayout.visibility = View.GONE
        binding.orderDetail.orderDetail.visibility = View.VISIBLE
        binding.orderDetail.itemLabel.text = getString(R.string.bin_id)
        binding.orderDetail.orderId.text = binNumber

        binding.toolBar1.toolbarTitle.text = getString(R.string.bin_details)
        binding.proceedBtn.text = getString(R.string.close_bin)

        binding.proceedBtn.isEnabled = true
        binding.proceedBtn.setBackgroundColor(ContextCompat.getColor(this, R.color.md_orange_800))


    }
}