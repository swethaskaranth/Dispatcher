package com.goflash.dispatch.ui.activity

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import com.goflash.dispatch.R
import com.goflash.dispatch.SortationApplication
import com.goflash.dispatch.api_services.SessionService
import com.goflash.dispatch.app_constants.INVALID_ACCESS
import com.goflash.dispatch.app_constants.REQUEST_CODE_IMMEDIATE_UPDATE
import com.goflash.dispatch.listeners.BarcodeScannerInterface
import com.goflash.dispatch.util.PreferenceHelper
import com.goflash.dispatch.util.httpErrorMessage
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.instana.android.Instana
import org.jetbrains.anko.indeterminateProgressDialog
import retrofit2.adapter.rxjava.HttpException
import java.io.IOException
import java.net.SocketTimeoutException


abstract class BaseActivity : AppCompatActivity() {

    private var dialog: Dialog? = null

    private lateinit var appUpdateManager: AppUpdateManager

    private var shouldScannerBeEnabled = true

    private lateinit var barcodeScanner: BarcodeScannerInterface

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initProgress()
        PreferenceHelper.init(this)
    }

    private fun initProgress() {
        dialog = indeterminateProgressDialog(resources.getString(R.string.waiting))
        dialog!!.setCanceledOnTouchOutside(false)
        dialog!!.setCancelable(false)
        dialog!!.dismiss()
    }

    override fun onStop() {
        super.onStop()
        //hideProgress()
    }

    protected fun showProgress() {
        hideKeyboard()
        if (!isFinishing && !isDestroyed)
            dialog?.show()
    }

    protected fun hideProgress() {
        try {
            if (dialog?.isShowing == true)
                dialog?.dismiss()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    protected fun hideKeyboard() {
        val view = currentFocus ?: return
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    protected fun processError(error: Throwable?) {
        when (error) {
            is HttpException -> handleHttpError(error)
            is SocketTimeoutException -> error(getString(R.string.request_timeout))
            is IOException -> error(getString(R.string.network_error))
            else -> error(error!!.message!!)
        }
    }

    private fun handleHttpError(error: Throwable) {

        val message = httpErrorMessage(error) ?: getString(R.string.unable_to_process)
        if (message == "401" || message == "403" || message == INVALID_ACCESS) {
            SessionService.token = ""
            PreferenceHelper.token = ""
            val builder = androidx.appcompat.app.AlertDialog.Builder(this)
                .setMessage(if (message == INVALID_ACCESS) INVALID_ACCESS else resources.getString(R.string.session_expired))
                .setPositiveButton(R.string.ok) { _, _ ->
                    onSuccessLogout()
                }
            builder.create().show()
        } else if (message == "system under Maintenance") {
            showMaintenanceAlert()
        } else
            error(message)
    }

    private fun onSuccessLogout() {
        hideProgress()
        openLoginActivity()
    }

    private fun openLoginActivity() {
        SessionService.token = ""
        PreferenceHelper.token = ""

        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finishAffinity()

    }

    fun showToast(context: Context, msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    fun showToastWithCenterGravity(context: Context, msg: String) {
        val toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT)
        toast.setGravity(Gravity.CENTER, 0, 0)
        toast.show()
    }

    protected open fun error(message: String) {
        if (message.contains("Version got expired"))
            versionUpdate(message)
        else
            errorMessage(message)
    }


    protected fun errorMessage(message: String) {
        val builder = AlertDialog.Builder(this)
            .setMessage(message)
            .setPositiveButton(R.string.ok) { _, _ ->
                shouldScannerBeEnabled = true
                enableScanner()
            }

        builder.create().show()
    }

    private fun openGooglePlay() {

        val uri = Uri.parse("https://play.google.com/store/apps/details?id=com.goflash.dispatch")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.android.vending")

        if (intent.resolveActivity(this.packageManager) != null) {
            startActivity(intent)
        } else {
            errorMessage("Play store not found.")
        }

    }

    private fun versionUpdate(message: String) {

        val builder = AlertDialog.Builder(this)
            .setMessage(message)
            .setPositiveButton(R.string.update) { _, _ -> updateApp() }

        builder.create().show()
    }

    private fun updateApp() {
        //PreferenceHelper.init(this)
        appUpdateManager = AppUpdateManagerFactory.create(this)
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            PreferenceHelper.updateType = "IMMEDIATE"
            appUpdateManager.startUpdateFlowForResult(
                appUpdateInfo,
                AppUpdateType.IMMEDIATE, //  HERE specify the type of update flow you want
                this,   //  the instance of an activity
                REQUEST_CODE_IMMEDIATE_UPDATE
            )
        }
    }

    override fun onResume() {
        super.onResume()
        //PreferenceHelper.init(this)
        if (PreferenceHelper.updateType == "IMMEDIATE") {
            appUpdateManager = AppUpdateManagerFactory.create(this)
            appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        AppUpdateType.IMMEDIATE, //  HERE specify the type of update flow you want
                        this,   //  the instance of an activity
                        REQUEST_CODE_IMMEDIATE_UPDATE
                    )
                } else
                    PreferenceHelper.updateType = ""
            }
        }

        if (shouldScannerBeEnabled)
            enableScanner()

        val packageManager: PackageManager = packageManager
        try {
            val info: ActivityInfo = packageManager.getActivityInfo(componentName, 0)
            Instana.view = info.name
            Log.d("app", "Activity name:" + info.name)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

    }

    protected fun getBold(value: String): String {
        return buildSpannedString {
            bold {
                append(value)
            }
        }.toString()
    }

    override fun onPause() {
        super.onPause()
        disableScanner()
    }


    protected fun enableScanner() {
        if (!SortationApplication.getSortationApplicationClass().getBarcodeReader().UIView!!) {
            if(this::barcodeScanner.isInitialized)
                barcodeScanner.initScanner()
            //SortationApplication.getSortationApplicationClass().getBarcodeReader().onResume()
        } else
            SortationApplication.getSortationApplicationClass().getBarcodeReader()
                .registerBroadcast(this)
    }

    protected fun disableScanner() {
        if (!SortationApplication.getSortationApplicationClass().getBarcodeReader().UIView!!) {
            SortationApplication.getSortationApplicationClass().getBarcodeReader().onPause()
            //barcodeView?.pause()
        } else {
            try {
                SortationApplication.getSortationApplicationClass().getBarcodeReader()
                    .unregisterBroadcast(this)
            } catch (ex: IllegalArgumentException) {
                ex.printStackTrace()
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    protected fun shouldEnableScanner(enable: Boolean) {
        shouldScannerBeEnabled = enable
    }

    fun showMaintenanceAlert() {

        val intent = Intent(this, MaintenanceActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)

    }

    fun initBarcodeScanner(scannerInterface: BarcodeScannerInterface){
        this.barcodeScanner = scannerInterface
    }


}
