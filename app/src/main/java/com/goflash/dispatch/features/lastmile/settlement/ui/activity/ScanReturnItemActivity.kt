package com.goflash.dispatch.features.lastmile.settlement.ui.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.annotation.RequiresApi
import androidx.camera.core.TorchState
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.goflash.dispatch.R
import com.goflash.dispatch.SortationApplication
import com.goflash.dispatch.app_constants.*
import com.goflash.dispatch.data.Item
import com.goflash.dispatch.databinding.LayoutSettlementScanItemBinding
import com.goflash.dispatch.di.components.DaggerActivityComponents
import com.goflash.dispatch.features.lastmile.settlement.listeners.ReviewItemListener
import com.goflash.dispatch.features.lastmile.settlement.presenter.ScanReturnItemPresenter
import com.goflash.dispatch.features.lastmile.settlement.ui.adapter.ScannedItemAdapter
import com.goflash.dispatch.features.lastmile.settlement.view.ScanReturnItemView
import com.goflash.dispatch.listeners.BarcodeScannerInterface
import com.goflash.dispatch.type.ReconStatus
import com.goflash.dispatch.ui.activity.ScannerBaseActivity
import com.goflash.dispatch.ui.itemDecoration.MarginItemDecoration
import com.pharmeasy.barcode.BarcodeReader
import com.pharmeasy.barcode.Event
import com.pharmeasy.barcode.ModeSelectedListener
import com.pharmeasy.barcode.ScannerType
import javax.inject.Inject

class ScanReturnItemActivity : ScannerBaseActivity(), ScanReturnItemView, View.OnClickListener,
    ReviewItemListener, BarcodeScannerInterface {

    @Inject
    lateinit var mPresenter: ScanReturnItemPresenter

    private lateinit var binding: LayoutSettlementScanItemBinding

    private var modeSelected = false

    private var barcodeScanned = false

    private var tripId: Long? = null

    private var sprinter: String? = null

    private var refId: String? = null

    private var shipmentId: String? = null

    private var partialDelivery = false

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
        binding = LayoutSettlementScanItemBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tripId = intent.getLongExtra(trip_id, 0)
        sprinter = intent.getStringExtra(sprinter_name)
        refId = intent.getStringExtra(reference_id)
        shipmentId = intent?.getStringExtra(shipment_id)
        partialDelivery = intent?.getBooleanExtra(partial_delivery, false) ?: false

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
            .build().inject(this@ScanReturnItemActivity)

        mPresenter.onAttachView(this, this)

        mPresenter.setShipmentId(shipmentId!!, partialDelivery)

    }

    private fun initViews() {
        binding.toolBar.toolbarTitle.text = getString(R.string.received_picked)
        binding.toolBar.tvSprinter.text = String.format(getString(R.string.trip_id_with_sprinter), tripId, sprinter)

        val label =
            SpannableString(String.format(getString(R.string.scan_items_in_shipment), refId))
        label.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(this, R.color.background_yellow)),
            14,
            label.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        binding.labelScan.text = label

        binding.btnPaymentLayout.btnPayment.text = getString(R.string.review_items)

        binding.rvItems.layoutManager = LinearLayoutManager(this)
        binding.rvItems.addItemDecoration(
            MarginItemDecoration(
                resources.getDimension(R.dimen.margin_10).toInt()
            )
        )

        binding.toolBar.ivBack.setOnClickListener(this)
        binding.btnPaymentLayout.btnPayment.setOnClickListener(this)
        binding.scanContentLayout.scanContentCamera.imgFlashlightOn.setOnClickListener(this)
        binding.scanContentLayout.scanContentCamera.imgFlashlightOff.setOnClickListener(this)
        binding.scanContentLayout.scanContent.imgScanner.setOnClickListener(this)
        binding.scanContentLayout.scanContentCamera.imgScannerCamera.setOnClickListener(this)

        mPresenter.getItems()

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
            mPresenter.onBarcodeScanned(barcode)
        }
    }


    override fun onPause() {

        super.onPause()

        if (!modeSelected)
            SortationApplication.getSortationApplicationClass().getBarcodeReader().clearMode()


    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.ivBack -> finish()
            R.id.btn_payment -> startItemSummaryActivity()
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

    override fun onFailure(error: Throwable?) {
        hideProgress()
        processError(error)
        barcodeScanned = false
    }

    override fun onScannedItemsFetched(list: List<Item>) {
        binding.rvItems.adapter = ScannedItemAdapter(this, list, this, partialDelivery)
        barcodeScanned = false
    }

    override fun onAcceptOrRejectSelected(
        position: Int,
        reconStatus: ReconStatus,
        reconReason: String,
        rejectRemarks: String
    ) {
        mPresenter.setAcceptReject(position, reconStatus, reconReason, rejectRemarks)
    }

    private fun startItemSummaryActivity() {
        val intent = Intent(this, ItemSummaryActivity::class.java)
        intent.putExtra(shipment_id, shipmentId)
        intent.putExtra(trip_id, tripId)
        intent.putExtra(sprinter_name, sprinter)
        intent.putExtra(reference_id, refId)
        intent.putExtra(partial_delivery, partialDelivery)
        startActivity(intent)

    }

    private fun startStep2Activity() {
        val intent = Intent(this, Step2PickedUpActivity::class.java)
        intent.putExtra(refresh, true)
        intent.putExtra(trip_id, tripId)
        intent.putExtra(sprinter_name, sprinter)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
    }


    override fun setItemCount(scanned: Int, total: Int) {
        binding.tvTaskCount.text = "$scanned/$total"
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

    override fun onDestroy() {
        super.onDestroy()
        mPresenter.onDetachView()
    }

}