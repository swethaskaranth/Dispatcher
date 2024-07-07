package com.goflash.dispatch.features.dispatch.ui.activity

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintJob
import android.print.PrintManager
import android.text.InputType
import android.view.MenuItem
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
import com.goflash.dispatch.data.Invoice
import com.goflash.dispatch.databinding.ActivityDispatchVehicleBinding
import com.goflash.dispatch.di.components.DaggerActivityComponents
import com.goflash.dispatch.features.dispatch.presenter.DispatchVehiclePresenter
import com.goflash.dispatch.features.dispatch.ui.adapter.InvoiceAdapter
import com.goflash.dispatch.features.dispatch.ui.interfaces.InvoiceListController
import com.goflash.dispatch.features.dispatch.view.DispatchVehicleView
import com.goflash.dispatch.listeners.BarcodeScannerInterface
import com.goflash.dispatch.service.PrintJobMonitorService
import com.goflash.dispatch.ui.activity.HomeActivity
import com.goflash.dispatch.ui.activity.ScannerBaseActivity
import com.goflash.dispatch.ui.interfaces.OnPrintFinishListener
import com.goflash.dispatch.util.hasFlash
import com.pharmeasy.barcode.BarcodeReader
import com.pharmeasy.barcode.Event
import com.pharmeasy.barcode.ModeSelectedListener
import com.pharmeasy.barcode.ScannerType
import com.pharmeasy.bolt.ui.adapters.CustomPrintDocumentAdapter
import java.util.regex.Pattern
import javax.inject.Inject

class DispatchVehicleActivity : ScannerBaseActivity(), DispatchVehicleView, View.OnClickListener,
    OnPrintFinishListener, BarcodeScannerInterface {

    @Inject
    lateinit var dispatchVehiclePresenter: DispatchVehiclePresenter

    private var mgr: PrintManager? = null

    private var tripCreated = false

    private var barcodeScanned = false

    private lateinit var binding: ActivityDispatchVehicleBinding

    private var modeSelected = false

    val listener = object : ModeSelectedListener {
        override fun onModeSelected(mode: String) {
            modeSelected = true
            when (mode) {
                ScannerType.BLUETOOTH_SCANNER.displayName -> {
                    binding.scanContentMain.scanContent.scanContent.visibility = View.VISIBLE
                    binding.scanContentMain.scanContentCamera.scanTask.visibility = View.GONE
                    binding.scanContentMain.etScan.visibility = View.GONE
                }
                ScannerType.OTG_SCANNER.displayName -> {
                    binding.scanContentMain.scanContent.scanContent.visibility = View.VISIBLE
                    binding.scanContentMain.scanContentCamera.scanTask.visibility = View.GONE
                    binding.scanContentMain.etScan.visibility = View.VISIBLE
                    binding.scanContentMain.etScan.requestFocus()
                }
                ScannerType.CAMERA_SCANNER.displayName -> {
                    binding.scanContentMain.scanContent.scanContent.visibility = View.GONE
                    binding.scanContentMain.scanContentCamera.scanTask.visibility = View.VISIBLE
                    binding.scanContentMain.etScan.visibility = View.GONE
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDispatchVehicleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mgr = getSystemService(PRINT_SERVICE) as PrintManager

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
            .build().inject(this@DispatchVehicleActivity)

        dispatchVehiclePresenter.onAttachView(this, this)

        dispatchVehiclePresenter.sendIntent(intent)
    }

    private fun initViews() {
        binding.scanContentMain.binName.visibility = View.GONE
        binding.scanContentMain.scanContent.scanLabel.text = getString(R.string.scan_seal_barcode)

        binding.scanContentMain.scanContent.barcodeLayout.imageView.setImageResource(R.drawable.ic_vehicle_barcode)

        binding.proceedBtn.setOnClickListener(this)
        binding.printManifest.setOnClickListener(this)

        binding.toolBar1.iVProfileHome.setOnClickListener(this)

        if (!hasFlash(applicationContext)) {
            binding.scanContentMain.scanContentCamera.imgFlashlightOn.visibility = View.GONE
        }

        binding.scanContentMain.scanContentCamera.imgFlashlightOn.setOnClickListener(this)
        binding.scanContentMain.scanContentCamera.imgFlashlightOff.setOnClickListener(this)
        binding.scanContentMain.scanContent.imgScanner.setOnClickListener(this)
        binding.scanContentMain.scanContentCamera.imgScannerCamera.setOnClickListener(this)

    }

    override fun initScanner() {
        initBarcodeScanner(this)
        if (!SortationApplication.getSortationApplicationClass().getBarcodeReader().UIView!!) {

            SortationApplication.getSortationApplicationClass().getBarcodeReader()
                .initializeScanner(this, intent, R.id.zxing_scanner_view, binding.scanContentMain.etScan, listener)

            (application as SortationApplication).getBarcodeReader().setTorchListener { state ->
                if (state == TorchState.ON) {
                    binding.scanContentMain.scanContentCamera.imgFlashlightOn.setImageResource(
                        R.drawable.ic_flash_off_black_24dp
                    )
                } else {
                    binding.scanContentMain.scanContentCamera.imgFlashlightOn.setImageResource(
                        R.drawable.ic_flash_on_black_24dp
                    )
                }
            }
        } else {
            binding.scanContentMain.scanContent.imgScanner.visibility = View.GONE
        }

        binding.scanContentMain.etScan.inputType = InputType.TYPE_NULL


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
            dispatchVehiclePresenter.onBarcodeScanned(barcode)
        } else
            showALertDialog(barcode)
        /*  }
      } else {
          showToast(this, "Trip is already created")
      }*/
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.printManifest -> dispatchVehiclePresenter.getConsolidatedManifest()
            R.id.proceed_btn -> {
                showProgress()
                onPrintSuccess() }
            R.id.iVProfileHome ->
                if (tripCreated)
                    onPrintSuccess()
                else
                    finish()

            R.id.img_flashlight_on -> {
                (application as SortationApplication).getBarcodeReader().toggleTorch()
            }

            R.id.img_scanner -> {
                SortationApplication.getSortationApplicationClass().getBarcodeReader()
                    .selectScanner(this, intent, R.id.zxing_scanner_view, binding.scanContentMain.etScan, listener)
            }
            R.id.img_scanner_camera -> {
                SortationApplication.getSortationApplicationClass().getBarcodeReader()
                    .selectScanner(this, intent, R.id.zxing_scanner_view, binding.scanContentMain.etScan, listener)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        dispatchVehiclePresenter.onDetachView()
    }

    override fun onFailure(error: Throwable?) {
        hideProgress()
        processError(error)
    }

    override fun onSuccess(message: String, invoiceRequired: Boolean) {
        hideProgress()
        tripCreated = true
        setupProceedButton(invoiceRequired, true)
        showToast(this, message)
        enableHomeButton()
        if (invoiceRequired)
            dispatchVehiclePresenter.getInvoiceList()
        //dispatchVehiclePresenter.getConsolidatedManifest()
    }

    override fun setBarcodeScanned(isbarcodeScanned: Boolean) {
        barcodeScanned = isbarcodeScanned
    }


    override fun enableordisableScanner(
        enable: Boolean,
        tripId: String,
        sprinter: String,
        time: String
    ) {

        if (enable) {
            binding.scanContentMain.scanDeliveryLabel.text = getString(R.string.scan_vehicle_seal)
            binding.toolBar1.toolbarTitle.text = getString(R.string.scan_vehicle_seal)
            binding.scanContentMain.scanContentMain.visibility = View.VISIBLE
            binding.tripDetail.layoutOptionalVehicleSeal.visibility = View.GONE

        } else {
            binding.tripDetail.tvLabel.text = getString(R.string.trip_id)
            binding.toolBar1.toolbarTitle.text = getString(R.string.dispatched_trip)

            binding.scanContentMain.scanContentMain.visibility = View.GONE
            binding.tripDetail.layoutOptionalVehicleSeal.visibility = View.VISIBLE
            binding.tripDetail.tvTripId.text = tripId
            binding.tripDetail.tvDriver.text = "${sprinter},"
            binding.tripDetail.tvTime.text = time

            tripCreated = true
        }
    }

    override fun setupProceedButton(invoiceRequired: Boolean, enable: Boolean) {
        binding.proceedBtn.text = getString(R.string.complete_task)

        if (enable) {
            binding.proceedBtn.visibility = View.VISIBLE
            binding.proceedBtn.isEnabled = true
            binding.proceedBtn.setBackgroundColor(ContextCompat.getColor(this, R.color.md_orange_800))
            binding.printManifest.visibility = View.VISIBLE
        } else {
            binding.proceedBtn.visibility = View.GONE
            binding.printManifest.visibility = View.GONE
        }
    }

    override fun printFromUrl(name: String, url: String) {
        print(
            name,
            CustomPrintDocumentAdapter(applicationContext, url, this),
            PrintAttributes.Builder().build()
        )
    }

    private fun print(
        name: String, adapter: PrintDocumentAdapter,
        attrs: PrintAttributes
    ): PrintJob {

        startService(Intent(this, PrintJobMonitorService::class.java))

        return mgr!!.print(name, adapter, attrs)
    }

    override fun onPrintFinished() {
        dispatchVehiclePresenter.onPrintFinished()
    }

    override fun onPrintSuccess() {
        hideProgress()
        val intent = Intent(this@DispatchVehicleActivity, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
    }

    override fun enableHomeButton() {
        binding.toolBar1.iVProfileHome.visibility = View.VISIBLE
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                if (!tripCreated)
                    finish()
                else
                    showToast(this, "Cannot go back")
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        if (tripCreated)
            showToast(this, "Cannot go back")
        else
            super.onBackPressed()
    }

    private fun showALertDialog(barcode: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.create_trip_title))
        builder.setMessage(String.format(getString(R.string.create_trip_message), barcode))
        builder.setPositiveButton(getString(R.string.ok)) { i1, i2 ->
            showProgress()
            dispatchVehiclePresenter.onBarcodeScanned(barcode)
        }
        builder.setNegativeButton(getString(R.string.cancel)) { i1, i2 ->
            //barcodeScanned = false
            enableScanner()
        }
        builder.show()
    }

    override fun onListFetched(list: ArrayList<Invoice>) {
        binding.tripDetail.invoiceList.layoutManager = LinearLayoutManager(this)
        binding.tripDetail.invoiceList.adapter = InvoiceAdapter(this, dispatchVehiclePresenter as InvoiceListController)
    }
}