package com.goflash.dispatch.features.receiving.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
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
import co.uk.rushorm.core.RushSearch
import com.goflash.dispatch.R
import com.goflash.dispatch.SortationApplication
import com.goflash.dispatch.app_constants.*
import com.goflash.dispatch.data.ReceivingDto
import com.goflash.dispatch.data.VehicleDetails
import com.goflash.dispatch.databinding.ActivityVehicleScanBinding
import com.goflash.dispatch.di.components.DaggerActivityComponents
import com.goflash.dispatch.features.receiving.presenter.VehicleScanPresenter
import com.goflash.dispatch.features.receiving.view.VehicleScanView
import com.goflash.dispatch.listeners.BarcodeScannerInterface
import com.goflash.dispatch.ui.activity.ScannerBaseActivity
import com.goflash.dispatch.util.getAlbumStorageDir
import com.goflash.dispatch.util.hasFlash
import com.pharmeasy.barcode.BarcodeReader
import com.pharmeasy.barcode.Event
import com.pharmeasy.barcode.ModeSelectedListener
import com.pharmeasy.barcode.ScannerType
import java.io.File
import java.io.IOException
import javax.inject.Inject

class VehicleScanActivity : ScannerBaseActivity(), VehicleScanView, View.OnClickListener,
    BarcodeScannerInterface {

    @Inject
    lateinit var mPresenter: VehicleScanPresenter

    private val PREVIEW = 1
    private var currentPhotoPath: String? = ""

    private var vehicleId: String? = null
    private var tripId: String? = null

    private lateinit var binding: ActivityVehicleScanBinding

    private var modeSelected = false

    private var sealScanned = false

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
                    //barcodeView?.setTorchListener(this@VehicleScanActivity)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVehicleScanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initScanner()
        initDagger()
        initViews()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermissions()
        }
    }

    private fun initDagger() {
        DaggerActivityComponents.builder()
            .networkComponent((application as SortationApplication).getNetworkComponent())
            .build().inject(this@VehicleScanActivity)

        mPresenter.onAttachView(this, this)
    }

    private fun initViews() {
        binding.txtTakePhoto.setOnClickListener(this)
        binding.btnProceed.setOnClickListener(this)
        binding.ivPreview.setOnClickListener(this)

        binding.toolBar1.toolbarTitle.text = getString(R.string.scan_vehicle)
        binding.scanContentLayout.scanContent.scanLabel.text = getString(R.string.scan_seal_barcode)

        vehicleId = intent.getStringExtra(VEHICLEID)
        tripId = intent.getStringExtra(TRIPID)

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


    override fun onPause() {

        if (!modeSelected)
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

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.txt_take_photo -> showPreviewActivity()
            R.id.iv_preview -> showPreviewActivity()
            R.id.btn_proceed -> mPresenter.uploadFile(
                File(
                    getAlbumStorageDir(
                        this@VehicleScanActivity,
                        album_name
                    ), currentPhotoPath!!
                ), vehicleId!!
            )
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_CANCELED && requestCode == PREVIEW && data?.getStringExtra(
                "filePath"
            ) != null
        ) {
            try {
                val mImageBitmap = MediaStore.Images.Media.getBitmap(
                    this.contentResolver, Uri.fromFile(
                        File(
                            getAlbumStorageDir(this@VehicleScanActivity, album_name),
                            data.getStringExtra("filePath")
                        )
                    )
                )
                currentPhotoPath = data.getStringExtra("filePath")
                binding.ivPreview.setImageBitmap(mImageBitmap)
                showPreview()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun showPreview() {
        binding.llTakeImage.visibility = View.GONE
        binding.clVehicleSealLost.visibility = View.VISIBLE
        binding.btnProceed.visibility = View.VISIBLE
    }

    private fun showPreviewActivity() {
        val intent = Intent(this, PreviewActivity::class.java)
        startActivityForResult(intent, PREVIEW)
    }

    private fun showReceiveBagActivity() {
        val intent = Intent(this, ReceiveBagActivity::class.java)
        intent.putExtra(VEHICLEID, vehicleId)
        intent.putExtra(TRIPID, tripId)
        startActivity(intent)
        finish()
    }

    override fun onBarcodeScanned(barcode: String) {

        shouldEnableScanner(false)
        disableScanner()

        if (barcode != vehicleId) {
            error("Please scan valid Vehicle Id / If seal is broken please take image.")
            return
        }

        mPresenter.verifyVehicleSeal(barcode, tripId)
    }

    override fun onFailure(error: Throwable?) {
        hideProgress()

        if (error != null)
            processError(error)
    }

    override fun onSuccess(bagDetails: MutableList<VehicleDetails>) {
        hideProgress()

        sealScanned = true

        bagDetails.forEach {
            it.vehicleId = vehicleId
            it.tripId = tripId
            it.save()
        }

        val receivingDto =
            RushSearch().whereEqual(VEHICLEID, vehicleId).findSingle(ReceivingDto::class.java)
        receivingDto.status = "PROCESSED"
        receivingDto.save()

        showReceiveBagActivity()
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
}
