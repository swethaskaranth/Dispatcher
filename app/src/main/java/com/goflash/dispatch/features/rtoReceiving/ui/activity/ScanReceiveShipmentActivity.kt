package com.goflash.dispatch.features.rtoReceiving.ui.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import co.uk.rushorm.core.RushCore
import com.goflash.dispatch.R
import com.goflash.dispatch.SortationApplication
import com.goflash.dispatch.app_constants.CAMERA_REQ
import com.goflash.dispatch.app_constants.run_id
import com.goflash.dispatch.data.InwardRunItem
import com.goflash.dispatch.data.PackageDto
import com.goflash.dispatch.data.ReceivingShipmentDTO
import com.goflash.dispatch.databinding.LayoutScanReceiveShipmentsBinding
import com.goflash.dispatch.di.components.DaggerActivityComponents
import com.goflash.dispatch.features.rtoReceiving.listeners.ConfirmCompleteListener
import com.goflash.dispatch.features.rtoReceiving.listeners.EnterBarcodeListener
import com.goflash.dispatch.features.rtoReceiving.listeners.RaiseIssueListener
import com.goflash.dispatch.features.rtoReceiving.listeners.ScanItemSelectionListener
import com.goflash.dispatch.features.rtoReceiving.presenter.ScanReceiveShipmentPresenter
import com.goflash.dispatch.features.rtoReceiving.ui.adapter.ScannedItemAdapter
import com.goflash.dispatch.features.rtoReceiving.ui.fragments.BottomSheetConfirmFragment
import com.goflash.dispatch.features.rtoReceiving.ui.fragments.BottomSheetEnterBarcodeFragment
import com.goflash.dispatch.features.rtoReceiving.ui.fragments.BottomSheetReasonsFragment
import com.goflash.dispatch.features.rtoReceiving.view.ScanReceiveShipmentView
import com.goflash.dispatch.ui.activity.ScannerBaseActivity
import com.goflash.dispatch.ui.itemDecoration.VerticalMarginItemDecoration
import com.goflash.dispatch.util.PreferenceHelper
import com.pharmeasy.barcode.BarcodeReader
import com.pharmeasy.barcode.Event
import com.pharmeasy.barcode.ModeSelectedListener
import com.pharmeasy.barcode.ScannerType
import javax.inject.Inject
import androidx.activity.result.contract.ActivityResultContract
import androidx.camera.core.TorchState
import com.goflash.dispatch.listeners.BarcodeScannerInterface
import com.goflash.dispatch.util.registerForActivityResult

class ScanReceiveShipmentActivity : ScannerBaseActivity(), ScanReceiveShipmentView,
    View.OnClickListener,  ScanItemSelectionListener,
    RaiseIssueListener, EnterBarcodeListener, ConfirmCompleteListener, BarcodeScannerInterface {

    var resultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val shipment = data?.getSerializableExtra("shipment") as ReceivingShipmentDTO
                mPresenter.onSortationComplete(shipment)
                initScanner()
            }
        }

    @Inject
    lateinit var mPresenter: ScanReceiveShipmentPresenter

    lateinit var binding: LayoutScanReceiveShipmentsBinding

    private var modeSelected = false

    private lateinit var adapter: ScannedItemAdapter

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
                    //barcodeView?.setTorchListener(this@ScanReceiveShipmentActivity)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = LayoutScanReceiveShipmentsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        PreferenceHelper.init(this)

        initScanner()
        initViews()
        initDagger()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermissions()
        }

    }

    private fun initDagger() {
        DaggerActivityComponents.builder()
            .networkComponent((application as SortationApplication).getNetworkComponent()).build()
            .inject(this@ScanReceiveShipmentActivity)
        mPresenter.onAttach(this, this)
        mPresenter.sendIntent(intent)
    }

    private fun initViews() {
        binding.toolbar.toolbarTitle.text = getString(R.string.receive_shipments)
        binding.toolbar.iVProfileHome.setOnClickListener(this)

        binding.scanContentLayout.scanContent.scanLabel.text =
            getString(R.string.scan_awb_lbn_refId)
        binding.scanContentLayout.scanContentCamera.imgFlashlightOn.setOnClickListener(this)
        binding.scanContentLayout.scanContentCamera.imgFlashlightOff.setOnClickListener(this)
        binding.scanContentLayout.scanContentCamera.imgScannerCamera.setOnClickListener(this)
        binding.scanContentLayout.scanContent.imgScanner.setOnClickListener(this)

        binding.rvScannedItems.layoutManager = LinearLayoutManager(this)

        adapter = ScannedItemAdapter(this, this)
        binding.rvScannedItems.adapter = adapter

        binding.rvScannedItems.addItemDecoration(
            VerticalMarginItemDecoration(
                resources.getDimension(
                    R.dimen.margin_13
                ).toInt()
            )
        )

        binding.btnStart.setOnClickListener(this)

        binding.btnStart.isEnabled = false
        binding.btnStart.setBackgroundResource(R.drawable.disable_button)

        binding.tvClickHere.setOnClickListener(this)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        mPresenter.getInwardRunItems()
    }

    override fun onBarcodeScanned(barcode: String) {
        showProgress()
        mPresenter.onBarcodeScanned(barcode)
    }

    override fun onActionButtonClicked(position: Int) {
        showProgress()
        mPresenter.getExceptionReasons(position)
    }

    override fun onShipmentCountClicked(position: Int) {
        mPresenter.getMpsRunItems(position)
    }

    override fun onInwardRunItemsFetched(items: List<InwardRunItem>) {
        hideProgress()
        adapter.setItemsList(items)
        updateScannedItemCount(items.size)

    }

    override fun updateScannedItemCount(count: Int) {
        binding.tvScannedItems.text =
            String.format(getString(R.string.scanned_shipments_count), count)
    }

    override fun onInwardRunItemAdded(item: InwardRunItem) {
        hideProgress()
        adapter.addItem(item)
    }

    override fun onItemRunChanged(item: InwardRunItem) {
        hideProgress()
        adapter.setItem(item)
    }

    override fun onExceptionsFetched(waybillNumber: String, status: String?, list: List<String>) {
        hideProgress()
        val bottomsheet = BottomSheetReasonsFragment()
        val bundle = Bundle()
        bundle.putString("wayBillNumber", waybillNumber)
        bundle.putString("status", status)
        bundle.putStringArrayList("exceptions", ArrayList(list))
        bottomsheet.arguments = bundle
        bottomsheet.show(supportFragmentManager, bottomsheet.tag)
    }

    override fun onStatusUpdated(runItem: InwardRunItem) {
        hideProgress()
        adapter.updateRunItem(runItem)
    }

    override fun onMpsRunItemsFetched(runId: Int, total: Int, items: List<InwardRunItem>) {
        val intent = Intent(this, ReceiveMpsShipmentListActvity::class.java)
        intent.putExtra(run_id, runId)
        intent.putExtra("total", total)
        intent.putExtra("mpsRunItems", items.toTypedArray())
        startActivity(intent)
    }

    override fun showBinName(scannedPackage: PackageDto, shipment: ReceivingShipmentDTO) {
        val intent = Intent(this, ScanBinActivity::class.java)
        intent.putExtra("scannedPackage", scannedPackage)
        intent.putExtra("shipment", shipment)
        resultLauncher.launch(intent)
    }

    override fun showProgressBar() {
        showProgress()
    }

    override fun disable() {
        disableScanner()
    }

    override fun enable() {
        enableScanner()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.iVProfileHome -> finish()
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
            R.id.btnStart -> {
                showCompleteConfirmBottomsheet()
            }
            R.id.tvClickHere -> showBarcodeEnterBottomSheet()

        }
    }

    override fun onResume() {
        super.onResume()

        mPresenter.getInwardRunItems()
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

    override fun onFailure(error: Throwable?) {
        hideProgress()
        processError(error)
    }

    override fun onSuccess(runId: Int) {
        hideProgress()
        RushCore.getInstance().clearDatabase()
        val intent = Intent(this, RunsheetActivity::class.java)
        intent.putExtra("home", true)
        intent.putExtra(run_id, runId)
        startActivity(intent)
    }

    override fun onStatusSelected(status: String, wayBillNumber: String, exceptions: List<String>) {
        showProgress()
        mPresenter.onReasonSelected(status, wayBillNumber, exceptions)
    }

    override fun enableComplete() {
        binding.btnStart.isEnabled = true
        binding.btnStart.setBackgroundResource(R.drawable.border_orange)
    }

    fun showBarcodeEnterBottomSheet() {
        val bottomsheet = BottomSheetEnterBarcodeFragment()
        bottomsheet.show(supportFragmentManager, bottomsheet.tag)
    }

    override fun onBarcodeEntered(barcode: String) {
        onBarcodeScanned(barcode)
    }

    private fun showCompleteConfirmBottomsheet() {
        val bottomSheet = BottomSheetConfirmFragment()
        bottomSheet.show(supportFragmentManager, bottomSheet.tag)
    }

    override fun onComplete() {
        showProgress()
        mPresenter.completeInwardRun()
    }

    override fun onDestroy() {
        super.onDestroy()
        mPresenter.onDetach()
    }

    override fun showReceivedMessage() {
        showToast(this, getString(R.string.received_at_final_destination))
    }
}