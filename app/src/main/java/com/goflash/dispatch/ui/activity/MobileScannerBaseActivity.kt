package com.goflash.dispatch.ui.activity

import android.os.Bundle
import android.util.Log
import com.symbol.emdk.EMDKManager
import com.symbol.emdk.EMDKResults
import com.symbol.emdk.barcode.*
import java.util.ArrayList

abstract class MobileScannerBaseActivity : BaseActivity(), EMDKManager.EMDKListener, Scanner.DataListener,
    Scanner.StatusListener, BarcodeManager.ScannerConnectionListener {

    private val TAG = MobileScannerBaseActivity::class.java.simpleName

    public var emdkManager: EMDKManager? = null
    private var barcodeManager: BarcodeManager? = null
    private var scanner: Scanner? = null

    private var deviceList: List<ScannerInfo>? = null

    private var bContinuousMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val results = EMDKManager.getEMDKManager(applicationContext, this)
        if (results.statusCode != EMDKResults.STATUS_CODE.SUCCESS) {
            //Log.d(TAG, "EMDKManager object request failed!")
            return
        }
    }

    protected abstract fun onBarcodeScanned(barcode: String)

    override fun onResume() {
        super.onResume()

        if (emdkManager != null) {
            //Log.d(TAG, "EMDKManager object request success!")
            barcodeManager = emdkManager!!.getInstance(EMDKManager.FEATURE_TYPE.BARCODE) as BarcodeManager

            // Add connection listener
            if (barcodeManager != null) {
                barcodeManager!!.addConnectionListener(this)
            }

            // Enumerate scanner devices
            enumerateScannerDevices()

            // Initialize scanner
            initScanner()
            setTrigger()
            setDecoders()

            startScan()

        }
    }

    override fun onPause() {
        super.onPause()
        // De-initialize scanner
        //Log.d(TAG, "EMDKManager object release")
        bContinuousMode = false
        deInitScanner()
        // Remove connection listener
        if (barcodeManager != null) {
            barcodeManager!!.removeConnectionListener(this)
            barcodeManager = null
            deviceList = null
        }

        // Release the barcode manager resources
        if (emdkManager != null) {
            emdkManager!!.release(EMDKManager.FEATURE_TYPE.BARCODE)
        }

    }

    private fun deInitScanner() {

        if (scanner != null) {

            try {

                scanner!!.disable()
                scanner!!.cancelRead()

            } catch (e: Exception) {
                e.printStackTrace()
            }

            try {

                scanner!!.removeStatusListener(this)
                scanner!!.removeDataListener(this)

            } catch (e: Exception) {
                e.printStackTrace()
            }
            //Log.d(TAG, "Scanner object release")
            try {
                scanner?.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            scanner = null
        }
    }

    private fun enumerateScannerDevices() {
        if (barcodeManager != null) {

            val friendlyNameList = ArrayList<String>()
            var spinnerIndex = 0

            deviceList = barcodeManager!!.supportedDevicesInfo

            if (deviceList != null && deviceList!!.isNotEmpty()) {

                val it = deviceList!!.iterator()
                while (it.hasNext()) {
                    val scnInfo = it.next()
                    friendlyNameList.add(scnInfo.friendlyName)
                    if (scnInfo.isDefaultScanner) {

                    }
                    ++spinnerIndex
                }
            }
        }
    }

    private fun initScanner() {
        if (scanner == null) {

            if (deviceList != null && deviceList!!.isNotEmpty()) {
                scanner = barcodeManager!!.getDevice(deviceList!![0])
            } else {
                //textViewStatus.setText("Status: " + "Failed to get the specified scanner device! Please close and restart the application.")
                return
            }

            if (scanner != null) {

                scanner!!.addDataListener(this)
                scanner!!.addStatusListener(this)

                try {
                    scanner!!.enable()
                } catch (e: ScannerException) {
                    //Log.d(TAG, "initScanner" + e.message)
                    //text_barcode.setText("Status: " + e.message)
                }
            } else {
                //textViewStatus.setText("Status: " + "Failed to initialize the scanner device.")
                //Log.d(TAG, "initScanner Failed to initialize the scanner device.")
            }
        }
    }

    private fun setTrigger() {
        if (scanner == null) {
            initScanner()
        }

        if (scanner != null) {
            when (0) {
                0 // Selected "HARD"
                -> scanner!!.triggerType = Scanner.TriggerType.HARD
                //-> scanner!!.triggerType = Scanner.TriggerType.SOFT_ALWAYS

            }
        }
    }

    private fun setDecoders() {
        //Log.d(TAG, "setDecoders")
        if (scanner == null) {
            initScanner()
        }

        if (scanner != null && scanner!!.isEnabled) {
            try {

                val config = scanner!!.config

                config.decoderParams.ean8.enabled = true

                config.decoderParams.ean13.enabled = true

                config.decoderParams.code39.enabled = true

                config.decoderParams.code128.enabled = true


                scanner!!.config = config

            } catch (e: ScannerException) {
                e.printStackTrace()
            }

        }
    }

    fun startScan() {

        if (scanner == null) {
            initScanner()
        }

        if (scanner != null) {
            try {

                if (scanner!!.isEnabled) {
                    // Submit a new read.
                    scanner!!.read()

                    bContinuousMode = true

                }

            } catch (e: ScannerException) {
                e.printStackTrace()
            }
        }
    }

    fun stopScan() {
        // De-initialize scanner
        bContinuousMode = false
        deInitScanner()
    }

    override fun onOpened(emdkManager: EMDKManager?) {
        this.emdkManager = emdkManager

        if (barcodeManager != null) {
            return
        }
        // Acquire the barcode manager resources
        barcodeManager = emdkManager!!.getInstance(EMDKManager.FEATURE_TYPE.BARCODE) as BarcodeManager?
        //Log.d(TAG, "emdkManager object onOpened")
        // Add connection listener
        if (barcodeManager != null) {
            barcodeManager!!.addConnectionListener(this)
            enumerateScannerDevices()
            startScan()
        }

    }

    override fun onClosed() {
        if (emdkManager != null) {

            // Remove connection listener
            if (barcodeManager != null) {
                barcodeManager!!.removeConnectionListener(this)
                barcodeManager = null
            }
            //Log.d(TAG, "emdkManager object onClosed")
            // Release all the resources
            emdkManager?.release(EMDKManager.FEATURE_TYPE.BARCODE)
        }
    }

    override fun onData(scanDataCollection: ScanDataCollection?) {
        if (scanDataCollection == null)
        return
        if (scanDataCollection.result != ScannerResults.SUCCESS)
            return
        val scanData = scanDataCollection.scanData
        for (data in scanData) {

            val dataString = data.data

            runOnUiThread {
                onBarcodeScanned(dataString.trim())
            }
        }
    }

    override fun onStatus(statusData: StatusData?) {
        //Log.d(TAG, "onStatus")
        val state = statusData!!.state
        when (state) {
            StatusData.ScannerStates.IDLE -> {

                if (bContinuousMode) {
                    try {
                        // An attempt to use the scanner continuously and rapidly (with a delay < 100 ms between scans)
                        // may cause the scanner to pause momentarily before resuming the scanning.
                        // Hence add some delay (>= 100ms) before submitting the next read.
                        try {
                            Thread.sleep(500) //before 100ms
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        }

                        scanner?.read()
                        //bContinuousMode = false
                    } catch (e: ScannerException) {
                        e.printStackTrace()
                    }
                }

            }
            StatusData.ScannerStates.WAITING -> {
                //Log.d(TAG, "Scanner is waiting for trigger press...")

            }
            StatusData.ScannerStates.SCANNING -> {
                //Log.d(TAG, "Scanning...")

            }
            StatusData.ScannerStates.DISABLED -> {
                //Log.d(TAG, "Disabled...")
            }
            StatusData.ScannerStates.ERROR -> {
                //Log.d(TAG, "errror occured")

            }
            else -> {
            }
        }
    }

    override fun onConnectionChange(p0: ScannerInfo?, p1: BarcodeManager.ConnectionState?) {

    }

}