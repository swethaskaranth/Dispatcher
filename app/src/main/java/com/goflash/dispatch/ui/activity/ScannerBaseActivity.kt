package com.goflash.dispatch.ui.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import com.goflash.dispatch.SortationApplication
import com.goflash.dispatch.scanner.ScannerActionListener
import com.goflash.dispatch.util.registerForActivityResult

abstract class ScannerBaseActivity : BaseActivity(), ScannerActionListener {

    private val ENABLE_BT = 1000

    private val TAG = ScannerBaseActivity::class.java.simpleName

    private var shouldScannerBeEnabled = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestMultiplePermissions.launch(arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT))
        }

    }


    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                Log.d("Scanner", "${it.key} = ${it.value}")
            }
        }

    protected abstract fun onBarcodeScanned(barcode: String)

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                checkConnectedDevice()
            } else {
                //message(getString(R.string.bt_not_enabled))
                finish()
            }
        }else
            super.onActivityResult(requestCode, resultCode, data)
    }

    private fun checkConnectedDevice(){

    }

    override fun onConnecting() {
        showProgress()
    }

    override fun onConnected() {
        hideProgress()
    }

    override fun onDisconnected() {
        hideProgress()
    }

    override fun onData(barcode: String) {
        onBarcodeScanned(barcode)
    }
}