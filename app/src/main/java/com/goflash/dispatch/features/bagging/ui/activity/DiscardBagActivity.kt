package com.goflash.dispatch.features.bagging.ui.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.pharmeasy.barcode.BarcodeReader
import com.pharmeasy.barcode.Event
import com.goflash.dispatch.R
import com.goflash.dispatch.SortationApplication
import com.goflash.dispatch.app_constants.CAMERA_REQ
import com.goflash.dispatch.app_constants.LABEL
import com.goflash.dispatch.app_constants.scannedBag
import com.goflash.dispatch.data.BagDTO
import com.goflash.dispatch.di.components.DaggerActivityComponents
import com.goflash.dispatch.features.bagging.presenter.DiscardBagPresenter
import com.goflash.dispatch.features.bagging.view.DiscardBagView
import com.goflash.dispatch.ui.activity.HomeActivity
import com.goflash.dispatch.ui.activity.ScannerBaseActivity
import com.goflash.dispatch.ui.interfaces.FragmentToActivityCommunicator
import com.goflash.dispatch.util.BottomSheetConfirmFragment
import com.goflash.dispatch.util.hasFlash
import com.pharmeasy.barcode.ModeSelectedListener
import android.text.InputType
import androidx.camera.core.TorchState
import com.goflash.dispatch.databinding.ActivityBagDetailBinding
import com.goflash.dispatch.listeners.BarcodeScannerInterface
import com.pharmeasy.barcode.ScannerType
import javax.inject.Inject

class DiscardBagActivity : ScannerBaseActivity(), DiscardBagView, View.OnClickListener,
    FragmentToActivityCommunicator, BarcodeScannerInterface {

    @Inject
    lateinit var discardBagPresenter: DiscardBagPresenter

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
                   // barcodeView?.setTorchListener(this@DiscardBagActivity)
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

    fun initDagger() {
        DaggerActivityComponents.builder()
            .networkComponent((application as SortationApplication).getNetworkComponent())
            .build().inject(this@DiscardBagActivity)

        discardBagPresenter.onAttachView(this, this@DiscardBagActivity)

        discardBagPresenter.sendIntent(intent)
    }

    fun initViews() {
        binding.chemistName.text = getString(R.string.scanned_shipments)
        binding.scanContentLayout.scanContent.scanLabel.text = getString(R.string.scan_seal_barcode)

        binding.proceedBtn.text = getString(R.string.discard_bag)
        binding.proceedBtn.isEnabled = false
        binding.proceedBtn.setBackgroundResource(R.drawable.disable_button)

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

        binding.toolBar1.toolbarTitle.text = getString(R.string.discard_bag)

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
            binding.scanContentLayout.scanContentCamera.imgScannerCamera.visibility = View.GONE
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

    override fun onSuccess(message: String) {
        hideProgress()
        showToast(this, message)
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
    }

    override fun onFailure(error: Throwable?) {
        hideProgress()
        processError(error)
    }

    override fun onDestroy() {
        super.onDestroy()
        discardBagPresenter.onDetachView()
    }

    override fun onBarcodeScanned(barcode: String) {
        discardBagPresenter.onBarcodeScanned(barcode)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.proceed_btn -> showBottomSheet()
            R.id.tv_view_details -> discardBagPresenter.getShipmentList()
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


    private fun showBottomSheet() {
        val bottomSheetConfirmFragment = BottomSheetConfirmFragment()
        val args = Bundle()
        args.putString(LABEL, getString(R.string.discard_bag_message))
        bottomSheetConfirmFragment.arguments = args
        bottomSheetConfirmFragment.show(supportFragmentManager, bottomSheetConfirmFragment.tag)

    }


    override fun setShipmentCount(count: String) {
        binding.scanCount.itemCount.text = count
    }

    override fun enableDiscardBtn() {
        binding.proceedBtn.isEnabled = true
        binding.proceedBtn.setBackgroundColor(ContextCompat.getColor(this, R.color.md_orange_800))

        showToast(this, getString(R.string.scan_sealed_successfully))
    }


    override fun onSuccess() {
        hideProgress()
        discardBagPresenter.discardBag()
    }

    override fun goToShipmentsActivity(bagDto: BagDTO) {
        val intent = Intent(this@DiscardBagActivity, ScannedShipmentsActivity::class.java)
        intent.putExtra(scannedBag, bagDto)
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

}