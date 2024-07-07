package com.goflash.dispatch.features.lastmile.tripCreation.ui.activity

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
import com.goflash.dispatch.app_constants.shipment_id
import com.goflash.dispatch.data.UnassignedDTO
import com.goflash.dispatch.databinding.LayoutSearchBarcodeBinding
import com.goflash.dispatch.di.components.DaggerActivityComponents
import com.goflash.dispatch.features.lastmile.tripCreation.listeners.FragmentListener
import com.goflash.dispatch.features.lastmile.tripCreation.listeners.UnassignedShipmentListener
import com.goflash.dispatch.features.lastmile.tripCreation.presenter.ScanToSearchPresenter
import com.goflash.dispatch.features.lastmile.tripCreation.ui.adapter.UnassignedAdapter
import com.goflash.dispatch.features.lastmile.tripCreation.ui.fragments.BottomSheetCancelFragment
import com.goflash.dispatch.features.lastmile.tripCreation.ui.fragments.BottomSheetUnblockFragment
import com.goflash.dispatch.features.lastmile.tripCreation.view.ScanToSearchView
import com.goflash.dispatch.listeners.BarcodeScannerInterface
import com.goflash.dispatch.model.AddressDTO
import com.goflash.dispatch.ui.activity.ScannerBaseActivity
import com.pharmeasy.barcode.BarcodeReader
import com.pharmeasy.barcode.Event
import com.pharmeasy.barcode.ModeSelectedListener
import com.pharmeasy.barcode.ScannerType
import org.jetbrains.anko.toast
import javax.inject.Inject

class ScanToSearchActivity : ScannerBaseActivity(), ScanToSearchView, View.OnClickListener,
    UnassignedShipmentListener, FragmentListener, BarcodeScannerInterface {


    @Inject
    lateinit var presenter: ScanToSearchPresenter

    private lateinit var binding: LayoutSearchBarcodeBinding

    private var mList = mutableListOf<UnassignedDTO>()
    private var mListHash = hashSetOf<UnassignedDTO>()
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
                    //barcodeView?.setTorchListener(this@ScanToSearchActivity)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LayoutSearchBarcodeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initScanner()
        initViews()
        initDagger()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermissions()
        }
    }

    private fun initViews() {

        binding.toolBar.toolbarTitle.text = getString(R.string.scan_barcode)

        binding.rvTasks.layoutManager = LinearLayoutManager(this)
        binding.rvTasks.adapter = UnassignedAdapter(this, mList, this)

        binding.fab.visibility = View.GONE
        binding.toolBar.iVProfileHome.setOnClickListener(this)
        binding.scanContentLayout.scanContentCamera.imgFlashlightOn.setOnClickListener(this)
        binding.scanContentLayout.scanContentCamera.imgFlashlightOff.setOnClickListener(this)
        binding.scanContentLayout.scanContent.imgScanner.setOnClickListener(this)
        binding.scanContentLayout.scanContentCamera.imgScannerCamera.setOnClickListener(this)
    }

    private fun initDagger() {
        DaggerActivityComponents.builder()
            .networkComponent((application as SortationApplication).getNetworkComponent()).build()
            .inject(this@ScanToSearchActivity)

        presenter.onAttach(this, this)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
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
        showProgress()
        val id= mList.filter { it.referenceId == barcode || it.packageId == barcode }.map { barcode }.contains(barcode)
        if(id){
            toast("Barcode Already Scanned")
            return
        }

        presenter.onBarcodeScanned(barcode)
    }

    /*override fun onTorchOn() {
        img_flashlight_on.visibility = View.GONE
        img_flashlight_off.visibility = View.VISIBLE

    }

    override fun onTorchOff() {
        img_flashlight_off.visibility = View.GONE
        img_flashlight_on.visibility = View.VISIBLE
    }*/

    override fun onSuccess(list: List<UnassignedDTO>) {
        hideProgress()

        mListHash = list.map { it }.toHashSet()
        mList.addAll(mListHash)
        binding.rvTasks.adapter?.notifyDataSetChanged()
    }

    override fun onFailure(error: Throwable?) {
        hideProgress()
        processError(error)
    }

    override fun updateList(position: Int, detail: AddressDTO) {
        hideProgress()

        mList[position].address1 = detail.address1
        mList[position].address2 = detail.address2
        mList[position].address3 = detail.address3
        mList[position].city = detail.city
        mList[position].state = detail.state
        mList[position].pincode = detail.pincode

        binding.rvTasks.adapter?.notifyDataSetChanged()
    }

    override fun onShipmentSelected(position: Int) {
        val frag = BottomSheetCancelFragment()
        frag.isCancelable = false
        frag.show(supportFragmentManager, frag.tag)
    }

    override fun deleteOrUnblockShipment(position: Int) {
        mList.remove(mList[position])
        binding.rvTasks.adapter?.notifyDataSetChanged()
        finish()
    }


    override fun onDeleteShipemnt(position : Int, data: UnassignedDTO) {

        val id = data.packageId ?: data.referenceId
        val intent = Intent(this, SelectCancelReason::class.java)
        intent.putExtra("referenceId", id)
        intent.putExtra("shipmentId", data.shipmentId)
        intent.putExtra("position", position)
        intent.putExtra("type",data.shipmentType)
        startActivity(intent)
    }

    override fun onEditShipment(position : Int, data: UnassignedDTO) {
        val frag = BottomSheetUnblockFragment()
        val args = Bundle()
        val id = data.packageId?:data.referenceId
        val shipId = data.packageId?:data.shipmentId
        args.putString("referenceId", id)
        args.putString("shipmentId", shipId)
        frag.arguments = args
        frag.isCancelable = false
        frag.show(supportFragmentManager, frag.tag)
    }

    override fun onViewDetails(position: Int, data: UnassignedDTO) {
        showProgress()
        presenter.getAddressDetails(position, data.shipmentId)
    }

    override fun onMpsCountClicked(position: Int) {
        val intent = Intent(this,MPSBoxesActivity::class.java)
        intent.putExtra(shipment_id, mList[position].shipmentId)
        val id = mList[position].packageId ?: mList[position].referenceId
        intent.putExtra("referenceId", id)
        intent.putExtra("boxCount",mList[position].mpsCount)
        startActivity(intent)
    }

    override fun callDeliveryAgent(phoneNumber: String) {

    }

    override fun commonListener() {

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

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDetach()
    }
}