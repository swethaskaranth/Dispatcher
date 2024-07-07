package com.goflash.dispatch.features.lastmile.settlement.ui.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.view.View
import androidx.annotation.RequiresApi
import androidx.camera.core.TorchState
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.goflash.dispatch.R
import com.goflash.dispatch.SortationApplication
import com.goflash.dispatch.app_constants.CAMERA_REQ
import com.goflash.dispatch.app_constants.sprinter_name
import com.goflash.dispatch.app_constants.trip_id
import com.goflash.dispatch.data.FmPickedShipment
import com.goflash.dispatch.databinding.LayoutReceiveFmPickupBinding
import com.goflash.dispatch.di.components.DaggerActivityComponents
import com.goflash.dispatch.features.lastmile.settlement.presenter.ReceiveFmPickupPresenter
import com.goflash.dispatch.features.lastmile.settlement.ui.adapter.FmPickupAdapter
import com.goflash.dispatch.features.lastmile.settlement.ui.fragments.BottomSheetShipmentScannedFragment
import com.goflash.dispatch.features.lastmile.settlement.view.ReceiveFmPickupView
import com.goflash.dispatch.listeners.BarcodeScannerInterface
import com.goflash.dispatch.ui.activity.ScannerBaseActivity
import com.pharmeasy.barcode.BarcodeReader
import com.pharmeasy.barcode.Event
import com.pharmeasy.barcode.ModeSelectedListener
import com.pharmeasy.barcode.ScannerType
import javax.inject.Inject

class ReceiveFmPickupShipmentActivity : ScannerBaseActivity(), View.OnClickListener, ReceiveFmPickupView,
    BarcodeScannerInterface {

    @Inject
    lateinit var mPresenter: ReceiveFmPickupPresenter

    private var modeSelected = false

    private var tripId: Long? = null

    private var sprinter: String? = null

    private var barcodeScanned = false

    private lateinit var binding: LayoutReceiveFmPickupBinding

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
                    //barcodeView?.setTorchListener(this@ReceiveFmPickupShipmentActivity)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LayoutReceiveFmPickupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (intent?.hasExtra(trip_id) == true) {
            tripId = intent.getLongExtra(trip_id, 0)
        } else if (intent?.hasExtra("tripId") == true) {
            tripId = intent.getStringExtra("tripId")?.toLong()
        }

        sprinter = intent.getStringExtra(sprinter_name)

        initScanner()
        initDagger()
        initViews()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermissions()
        }
    }

    private fun initViews() {
        binding.toolBar.toolbarTitle.text = getString(R.string.received_fm_picked)
        binding.toolBar.tvSprinter.text = String.format(getString(R.string.trip_id_with_sprinter), tripId, sprinter)

        binding.btnPaymentLayout.btnPayment.text = getString(R.string.proceed)

        binding.rvShipments.layoutManager = LinearLayoutManager(this)

        binding.toolBar.ivBack.setOnClickListener(this)
        binding.btnPaymentLayout.btnPayment.setOnClickListener(this)
        binding.scanContentLayout.scanContentCamera.imgFlashlightOn.setOnClickListener(this)
        binding.scanContentLayout.scanContentCamera.imgFlashlightOff.setOnClickListener(this)
        binding.scanContentLayout.scanContent.imgScanner.setOnClickListener(this)
        binding.scanContentLayout.scanContentCamera.imgScannerCamera.setOnClickListener(this)

        mPresenter.getShipments()
    }

    private fun initDagger() {
        DaggerActivityComponents.builder()
            .networkComponent((application as SortationApplication).getNetworkComponent())
            .build().inject(this@ReceiveFmPickupShipmentActivity)

        mPresenter.onAttachView(this, this)

        mPresenter.setTripId(tripId!!)

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
        if (!barcodeScanned) {
            barcodeScanned = true
            showProgress()
            mPresenter.onBarcodeScanned(barcode)
        }
    }

    override fun showAlreadyScanned(barcode: String) {
        val bottomSheetFragment = BottomSheetShipmentScannedFragment()
        val args = Bundle()
        args.putString("LABEL", barcode)
        bottomSheetFragment.arguments = args
        bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
        barcodeScanned = false
    }

    override fun onClick(v: View?) {
       when(v?.id){
           R.id.btn_payment -> startSummaryActivity()
           R.id.img_flashlight_on -> (application as SortationApplication).getBarcodeReader().toggleTorch()
       }
    }

    override fun onShipmentsFetched(list: List<FmPickedShipment>) {
        hideProgress()
        if(list.isNotEmpty())
            binding.layoutScanned.visibility = View.VISIBLE
        binding.rvShipments.adapter = FmPickupAdapter(this, list)
        barcodeScanned = false

    }

    override fun setShipmentCount(scanned: Int, total: Int) {
        binding.tvTaskCount.text = "$scanned/$total"
    }

    /*override fun onTorchOn() {
        binding.img_flashlight_on.visibility = View.GONE
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

    private fun startSummaryActivity(){
        val intent = Intent(this, FmSummaryActivity::class.java)
        intent.putExtra(trip_id, tripId)
        intent.putExtra(sprinter_name, sprinter)
        startActivity(intent)
    }

    override fun onFailure(error: Throwable?) {
        hideProgress()
        processError(error)
        barcodeScanned = false
    }

    override fun onDestroy() {
        super.onDestroy()
        mPresenter.onDetachView()
    }


}