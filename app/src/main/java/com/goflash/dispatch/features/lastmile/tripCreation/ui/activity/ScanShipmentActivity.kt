package com.goflash.dispatch.features.lastmile.tripCreation.ui.activity

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.text.InputType
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.camera.core.TorchState
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.goflash.dispatch.R
import com.goflash.dispatch.SortationApplication
import com.goflash.dispatch.app_constants.CAMERA_REQ
import com.goflash.dispatch.app_constants.shipment_id
import com.goflash.dispatch.app_constants.sprinter_name
import com.goflash.dispatch.app_constants.trip_id
import com.goflash.dispatch.data.TaskListDTO
import com.goflash.dispatch.databinding.LayoutScanBarcodeBinding
import com.goflash.dispatch.di.components.DaggerActivityComponents
import com.goflash.dispatch.features.lastmile.tripCreation.listeners.DataUpdateListener
import com.goflash.dispatch.features.lastmile.tripCreation.presenter.ScanShipmentPresenter
import com.goflash.dispatch.features.lastmile.tripCreation.ui.adapter.ShipmentAdapter
import com.goflash.dispatch.features.lastmile.tripCreation.ui.fragments.BottomSheetTripDeleteFragment
import com.goflash.dispatch.features.lastmile.tripCreation.view.ScanShipmentView
import com.goflash.dispatch.listeners.BarcodeScannerInterface
import com.goflash.dispatch.model.AddressDTO
import com.goflash.dispatch.type.ShipmentType
import com.goflash.dispatch.ui.activity.ScannerBaseActivity
import com.goflash.dispatch.ui.interfaces.FragmentToActivityCommunicator
import com.goflash.dispatch.ui.itemDecoration.MarginItemDecoration
import com.goflash.dispatch.util.RecyclerItemTouchHelper
import com.goflash.dispatch.util.hasFlash
import com.pharmeasy.barcode.BarcodeReader
import com.pharmeasy.barcode.Event
import com.pharmeasy.barcode.ModeSelectedListener
import com.pharmeasy.barcode.ScannerType
import javax.inject.Inject

class ScanShipmentActivity : ScannerBaseActivity(), ScanShipmentView, View.OnClickListener,
     RecyclerItemTouchHelper.RecyclerItemTouchHelperListener,
    FragmentToActivityCommunicator, DataUpdateListener, BarcodeScannerInterface {

    private val TAG = ScanShipmentActivity::class.java.simpleName

    @Inject
    lateinit var mPresenter: ScanShipmentPresenter

    private var tripId: Long? = null

    private var shipmentList = mutableListOf<TaskListDTO>()

    private lateinit var binding: LayoutScanBarcodeBinding

    private var modeSelected = false

    private var scannedBarcode: String? = null

    private var max_time = 3

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
                    binding.bgView.visibility = View.VISIBLE
                    //barcodeView?.setTorchListener(this@ScanShipmentActivity)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LayoutScanBarcodeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tripId = intent.getLongExtra(trip_id, 0)

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
            .build().inject(this@ScanShipmentActivity)

        mPresenter.onAttach(this, this@ScanShipmentActivity)
    }

    private fun initViews() {

        binding.toolBar.ivDelete.visibility = View.VISIBLE

        binding.toolBar.toolbarTitle.text = String.format(getString(R.string.trip_number, tripId))
        binding.toolBar.tvSprinter.visibility = View.VISIBLE
        binding.toolBar.tvSprinter.text = intent.getStringExtra(sprinter_name)
        binding.toolBar.ivDelete.visibility = View.VISIBLE
        binding.labelShipmentList.visibility = View.VISIBLE
        //   bgView.visibility = View.VISIBLE

        binding.fab.visibility = View.VISIBLE

        binding.rvTasks.layoutManager = LinearLayoutManager(this)
        binding.rvTasks.adapter = ShipmentAdapter(this, shipmentList, this)
        binding.rvTasks.addItemDecoration(
            MarginItemDecoration(
                resources.getDimension(R.dimen.margin_10).toInt()
            )
        )


        val itemTouchHelperCallback = RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this)
        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(binding.rvTasks)

        binding.fab.setOnClickListener(this)
        binding.toolBar.ivBack.setOnClickListener(this)
        binding.toolBar.ivDelete.setOnClickListener(this)

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

            SortationApplication.getSortationApplicationClass().getBarcodeReader().checkLastScannedValue(false)
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
        /*if (!isScanned) {
            isScanned = true*/
        disableScanner()
        scannedBarcode = barcode
        /*if (!SortationApplication.getSortationApplicationClass().getBarcodeReader().UIView!!) {
            scan_content_layout.visibility = View.GONE
        }*/
        if (checkToRemoveShipment(barcode)) {
            showAlertDialogToRemove(barcode)
        } else
            mPresenter.addShipment(barcode, tripId!!)
        //}

    }

    override fun onResume() {
        super.onResume()

        showProgress()
        mPresenter.getShipmentsForTrip(tripId!!)

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
        when (v!!.id) {
            R.id.ivBack -> finish()

            R.id.img_flashlight_on -> {
                (application as SortationApplication).getBarcodeReader().toggleTorch()
            }

            R.id.img_flashlight_off -> {
                //barcodeView?.setTorchOff()
            }
            R.id.img_scanner -> {
                SortationApplication.getSortationApplicationClass().getBarcodeReader()
                    .selectScanner(this, intent, R.id.zxing_scanner_view, binding.scanContentLayout.etScan, listener)
            }
            R.id.img_scanner_camera -> {
                SortationApplication.getSortationApplicationClass().getBarcodeReader()
                    .selectScanner(this, intent, R.id.zxing_scanner_view, binding.scanContentLayout.etScan, listener)
            }
            R.id.ivDelete -> {
                showDeleteTripBottomsheet()
            }
            R.id.fab -> {
                startAddShipemntsActivity()
            }
        }
    }

    override fun onShipmentsFetched(list: List<TaskListDTO>) {
        hideProgress()

        shipmentList.clear()
        shipmentList.addAll(list)
        binding.rvTasks.adapter?.notifyDataSetChanged()

        if (scannedBarcode != null) {
            val shipment =
                list.find { it.referenceId == scannedBarcode || it.packageId == scannedBarcode || it.lbn == scannedBarcode }
            if (shipment != null) {
                binding.scanContentLayout.scanContentLayout.visibility = View.GONE
                binding.priorityCountLayout.priorityCountLayout.visibility = View.VISIBLE
                startTimer()
            }
            scannedBarcode = null

        }else
            enableScanner()

        binding.tvTaskCount.visibility = View.VISIBLE

        binding.tvTaskCount.text = "${shipmentList.size}"
    }

    private fun startTimer() {

        val countDownTimer = object : CountDownTimer((max_time * 1000).toLong(), 100) {
            override fun onTick(millisUntilFinished: Long) {
                disableScanner()
            }

            override fun onFinish() {
                showScanner()
                enableScanner()
            }
        }

        countDownTimer.start()
    }

    private fun showScanner() {
        enableScanner()

        binding.scanContentLayout.scanContentCamera.statusPanel.visibility = View.INVISIBLE
        binding.priorityCountLayout.priorityCountLayout.visibility = View.GONE
        binding.scanContentLayout.scanContentLayout.visibility = View.VISIBLE
    }


    override fun onFailure(error: Throwable?) {
        scannedBarcode = null
        hideProgress()
        processError(error)
    }

    private fun checkToRemoveShipment(barcode: String): Boolean {
        return shipmentList.any { it.referenceId == barcode || it.packageId == barcode || it.lbn == barcode } ||
                shipmentList.mapNotNull { it.childShipments }.flatten().any { it.referenceId == barcode || it.packageId == barcode || it.lbn == barcode }
    }

    override fun onAddRemoveSuccess(add: Boolean) {
        showProgress()

        if (!add)
            scannedBarcode = null

        mPresenter.getShipmentsForTrip(tripId!!)
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int, position: Int) {
        showAlertDialogToRemove(shipmentList[position].referenceId)
    }

    override fun onMove(current: Int, target: Int) {

    }

    private fun showDeleteTripBottomsheet() {
        val frag = BottomSheetTripDeleteFragment()
        frag.isCancelable = false
        frag.show(supportFragmentManager, frag.tag)
    }

    override fun onSuccess() {
        showProgress()
        mPresenter.deleteTrip(tripId!!)
    }

    override fun onTripDeleteSuccess() {
        hideProgress()
        val intent = Intent(this, LastMileActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finishAffinity()
    }

    private fun startAddShipemntsActivity() {
        val intent = Intent(this, AddShipmentActivity::class.java)
        intent.putExtra(trip_id, tripId!!)
        startActivity(intent)
    }

    private fun showAlertDialogToRemove(shipmentId: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.remove_shipment_title))
        builder.setCancelable(false)
        builder.setMessage(String.format(getString(R.string.remove_shipment_message), shipmentId))
        builder.setPositiveButton(getString(R.string.ok)) { _, _ ->

            mPresenter.removeShipment(
                shipmentId,
                tripId!!
            )
        }
        builder.setNegativeButton(getString(R.string.cancel)) { _, _ ->
            binding.rvTasks.adapter?.notifyDataSetChanged()
            enableScanner()
        }
        builder.show()
    }

    override fun onViewDetails(position: Int, data: TaskListDTO) {
        showProgress()
        mPresenter.getAddressDetails(position, data.shipmentId)
    }

    override fun updateList(position: Int, detail: AddressDTO) {
        hideProgress()

        shipmentList[position].name = detail.name
        shipmentList[position].address1 = detail.address1
        shipmentList[position].address2 = detail.address2
        shipmentList[position].address3 = detail.address3
        shipmentList[position].city = detail.city
        shipmentList[position].state = detail.state
        shipmentList[position].pincode = detail.pincode?.toInt()

        binding.rvTasks.adapter?.notifyDataSetChanged()
    }

    override fun onDestroy() {
        super.onDestroy()
        mPresenter.onDetach()
    }

    override fun onItemSelected(position: Int) {
        val task = shipmentList[position]
        val intent = Intent(this,MPSBoxesActivity::class.java)
        intent.putExtra(shipment_id, task.shipmentId)
        intent.putExtra("referenceId", task.packageId?:task.referenceId)
        intent.putParcelableArrayListExtra("childShipments",ArrayList(task.childShipments))
        startActivity(intent)
    }

}
