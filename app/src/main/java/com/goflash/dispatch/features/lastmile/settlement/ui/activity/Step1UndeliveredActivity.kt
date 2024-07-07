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
import co.uk.rushorm.core.RushSearch
import com.goflash.dispatch.R
import com.goflash.dispatch.SortationApplication
import com.goflash.dispatch.app_constants.CAMERA_REQ
import com.goflash.dispatch.data.*
import com.goflash.dispatch.databinding.ActivityTripSettlementBinding
import com.goflash.dispatch.di.components.DaggerActivityComponents
import com.goflash.dispatch.features.lastmile.settlement.presenter.Step1Presenter
import com.goflash.dispatch.features.lastmile.settlement.ui.adapter.UndeliveredAdapter
import com.goflash.dispatch.features.lastmile.settlement.view.Step1View
import com.goflash.dispatch.listeners.BarcodeScannerInterface
import com.goflash.dispatch.ui.activity.ScannerBaseActivity
import com.pharmeasy.barcode.BarcodeReader
import com.pharmeasy.barcode.Event
import com.pharmeasy.barcode.ModeSelectedListener
import com.pharmeasy.barcode.ScannerType
import org.jetbrains.anko.toast
import javax.inject.Inject

class Step1UndeliveredActivity : ScannerBaseActivity(), Step1View, View.OnClickListener,
    BarcodeScannerInterface {


    @Inject
    lateinit var presenter: Step1Presenter

    private lateinit var binding: ActivityTripSettlementBinding

    private var mList = mutableListOf<UndeliveredShipmentDTO>()
    private var mListHash = hashSetOf<UndeliveredShipmentDTO>()

    private var modeSelected = false

    private var tripId: String? = null
    private var sprinterName: String? = null
    private var tripList: TripSettlementDTO? = null

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
                   // barcodeView?.setTorchListener(this@Step1UndeliveredActivity)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTripSettlementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initDagger()
        initScanner()
        initViews()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermissions()
        }
    }

    private fun initViews() {

        mList.clear()

        tripId = intent.getStringExtra("tripId")
        sprinterName = intent.getStringExtra("sprinterName")

        tripList = presenter.getUndeliveredData(tripId!!)

        binding.btnPaymentLayout.btnPayment.setOnClickListener(this)
        binding.toolBar.ivBack.setOnClickListener(this)

        binding.toolBar.toolbarTitle.text = getString(R.string.received_undelivered)
        binding.btnPaymentLayout.btnPayment.text = resources.getString(R.string.proceed)
        binding.toolBar.tvSprinter.text = "Trip ID #$tripId - $sprinterName"

        binding.rvTasks.layoutManager = LinearLayoutManager(this)
        //rvTasks.adapter = UndeliveredAdapter(this, mList)

        mList.clear()
        mListHash = RushSearch().whereChildOf(
            TripSettlementDTO::class.java,
            "undeliveredShipment",
            tripList?.id
        ).and()
            .whereEqual("scanned", true)
            .find(UndeliveredShipmentDTO::class.java).toHashSet()

        updateCount()

    }

    private fun initDagger() {
        DaggerActivityComponents.builder()
            .networkComponent((application as SortationApplication).getNetworkComponent()).build()
            .inject(this@Step1UndeliveredActivity)

        presenter.onAttach(this, this)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.ivBack -> finish()
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

            R.id.btn_payment -> showPart2()
        }
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


    override fun onPause() {


        if (!modeSelected)
            SortationApplication.getSortationApplicationClass().getBarcodeReader().clearMode()


        super.onPause()
    }

    override fun onBarcodeScanned(barcode: String) {
        presenter.onBarcodeScanned(barcode)
    }

//    override fun onTorchOn() {
//        img_flashlight_on.visibility = View.GONE
//        img_flashlight_off.visibility = View.VISIBLE
//
//    }
//
//    override fun onTorchOff() {
//        img_flashlight_off.visibility = View.GONE
//        img_flashlight_on.visibility = View.VISIBLE
//    }

    override fun onSuccess(data: UndeliveredShipmentDTO) {
        hideProgress()
        mListHash.add(data)
        updateCount()
    }

    override fun onFailure(error: Throwable?) {
        hideProgress()
        processError(error)
    }

    override fun showAlert(msg: String) {
        toast(msg)
    }

    private fun updateCount() {
        mList.clear()
        mList.addAll(mListHash)
        binding.rvTasks.adapter = UndeliveredAdapter(this, mList.sortedByDescending { it.updated })
        //rvTasks.adapter?.notifyDataSetChanged()
        binding.tvNumber.text =
            "${mListHash.size}/${presenter.getUndeliveredData(tripId!!).undeliveredShipment.size}"
    }

    private fun showPart2() {

        val scanned = RushSearch()
            .whereChildOf(TripSettlementDTO::class.java, "undeliveredShipment", tripList?.id)
            .find(UndeliveredShipmentDTO::class.java)

        val lostAndDamaged = RushSearch()
            .whereChildOf(TripSettlementDTO::class.java, "lostDamagedShipments", tripList?.id)
            .find(LostDamagedShipment::class.java)

        intent = if (scanned.any { !it.isScanned } || lostAndDamaged.isNotEmpty()) {
            Intent(this, Step1UnscannedActivity::class.java)

        } else {

            val trip = RushSearch().whereEqual("tripId", tripId!!.toLong())
                .findSingle(TripSettlementDTO::class.java)
            if (trip != null) {
                trip.isUndeliveredScanned = true
                trip.save()
            }

            if (tripList?.returnShipment!!.isEmpty()) {
                val fmShipments = RushSearch().whereEqual("tripId", tripId!!.toLong()).find(FmPickedShipment::class.java)
                if (fmShipments.isNullOrEmpty()) {
                    val ackslips = RushSearch().whereEqual("tripId", tripId!!.toLong()).find(AckSlipDto::class.java)
                    if(ackslips.isNullOrEmpty()) {

                        val deliverySlips = RushSearch().whereChildOf(
                            TripSettlementDTO::class.java, "poas",
                            trip!!.id
                        )
                            .find(PoaResponseForRecon::class.java)
                        if(deliverySlips.isNullOrEmpty())
                            Intent(this, Step3CashCollectionActivity::class.java)
                        else
                            Intent(this, AckDeliverySlipReconActivity::class.java)
                    }
                    else
                        Intent(this, Step4VerifyImagesActivity::class.java)
                }
                else
                    Intent(this, ReceiveFmPickupShipmentActivity::class.java)
            } else
                Intent(this, Step2PickedUpActivity::class.java)
        }

        intent.putExtra("tripId", tripId)
        intent.putExtra("sprinterName", sprinterName)
        startActivity(intent)
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

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDetach()
    }
}