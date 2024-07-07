package com.goflash.dispatch.features.dispatch.ui.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.Toolbar
import android.view.MenuItem
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import com.goflash.dispatch.R
import com.goflash.dispatch.SortationApplication
import com.goflash.dispatch.app_constants.CAMERA_REQ
import com.goflash.dispatch.app_constants.LABEL
import com.goflash.dispatch.app_constants.bagUpdated
import com.goflash.dispatch.app_constants.seal_required
import com.goflash.dispatch.di.components.DaggerActivityComponents
import com.goflash.dispatch.features.dispatch.presenter.DispatchBagPresenter
import com.goflash.dispatch.features.dispatch.view.DispatchBagView
import com.goflash.dispatch.ui.activity.ScannerBaseActivity
import com.goflash.dispatch.ui.interfaces.FragmentToActivityCommunicator
import com.goflash.dispatch.util.BottomSheetConfirmFragment
import com.goflash.dispatch.util.hasFlash
import com.pharmeasy.barcode.BarcodeReader
import com.pharmeasy.barcode.Event
import com.pharmeasy.barcode.ModeSelectedListener
import android.text.InputType
import androidx.camera.core.TorchState
import com.goflash.dispatch.databinding.ActivityBagDetailBinding
import com.goflash.dispatch.listeners.BarcodeScannerInterface
import com.pharmeasy.barcode.ScannerType
import javax.inject.Inject

class DispatchBagActivity : ScannerBaseActivity(), DispatchBagView, View.OnClickListener, FragmentToActivityCommunicator,
    BarcodeScannerInterface {

    @Inject
    lateinit var dispatchBagPresenter: DispatchBagPresenter

    private lateinit var binding: ActivityBagDetailBinding

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

    private fun initDagger() {
        DaggerActivityComponents.builder().networkComponent((application as SortationApplication).getNetworkComponent())
                .build().inject(this@DispatchBagActivity)

        dispatchBagPresenter.onAttachView(this, this@DispatchBagActivity)

    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if(intent?.getBooleanExtra(bagUpdated,false) == true)
            dispatchBagPresenter.getBagCount()

    }

    private fun initViews() {
        binding.chemistName.text = getString(R.string.scanned_bags)
        binding.scanContentLayout.scanContent.scanLabel.text = getString(R.string.scan_bag_barcode)

       binding.scanContentLayout.scanContent.barcodeLayout.imageView.setImageResource(R.drawable.ic_scan_bag_revised)

        binding.scanCount.itemsPickedFromCustomer.text = getString(R.string.no_of_bags_scanned)


        binding.proceedBtn.text = getString(R.string.proceed)
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

        binding.toolBar1.toolbarTitle.text = getString(R.string.add_bags)

        dispatchBagPresenter.getBagCount()

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

    override fun onBarcodeScanned(barcode: String) {
        dispatchBagPresenter.onBarcodeScanned(barcode)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.tv_view_details -> showScannedBagActivity()
            R.id.proceed_btn -> {
                showProgress()
                dispatchBagPresenter.getVehicleSealRequired()
            }
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

    override fun setBagCount(count: String) {
        binding.scanCount.itemCount.text = count
        if (count.toInt() > 0) {
            binding.proceedBtn.isEnabled = true
            binding.proceedBtn.setBackgroundColor(ContextCompat.getColor(this, R.color.md_orange_800))
        }
    }

    override fun onFailure(error: Throwable?) {
        hideProgress()
        processError(error)
    }


    override fun onDestroy() {
        super.onDestroy()
        dispatchBagPresenter.onDetachView()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                showConfirmBottomSheet()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showScannedBagActivity() {
        startActivity(Intent(this@DispatchBagActivity, ScannedBagsActivity::class.java))

    }

    private fun showVehicleDetailActivity(sealRequired : Boolean) {
        val intent = Intent(this, SizeSelectionActivity::class.java)
        intent.putExtra(seal_required,sealRequired)
        startActivity(intent)
    }

    override fun onBackPressed() {
        showConfirmBottomSheet()

    }

    private fun showConfirmBottomSheet(){
        val bottomSheetConfirmFragment = BottomSheetConfirmFragment()
        val args = Bundle()
        args.putString(LABEL,getString(R.string.discard_dispatch_message))
        bottomSheetConfirmFragment.arguments = args
        bottomSheetConfirmFragment.show(supportFragmentManager,bottomSheetConfirmFragment.tag)
    }

    override fun onSuccess() {
        super.onSuccess()
        hideProgress()
        dispatchBagPresenter.deleteData()
        finish()
    }

    override fun onSealRequiedFetched(sealRequired: Boolean) {
        hideProgress()
        showVehicleDetailActivity(sealRequired)
    }

}