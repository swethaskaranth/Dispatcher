package com.goflash.dispatch.ui.fragments

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.SpannableStringBuilder
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AlertDialog
import android.view.inputmethod.InputMethodManager
import androidx.core.app.ActivityCompat
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import com.goflash.dispatch.R
import com.goflash.dispatch.api_services.SessionService
import com.goflash.dispatch.api_services.SessionService.name
import com.goflash.dispatch.ui.activity.LoginActivity
import com.goflash.dispatch.util.PreferenceHelper
import com.goflash.dispatch.util.httpErrorMessage
import org.jetbrains.anko.indeterminateProgressDialog
import retrofit2.adapter.rxjava.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

abstract class BaseBottomSheetFragment : BottomSheetDialogFragment() {

    private var progressDialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initProgress()
    }

    private fun initProgress() {
        progressDialog = activity?.indeterminateProgressDialog(resources.getString(R.string.waiting))
        progressDialog?.setCanceledOnTouchOutside(false)
        progressDialog?.setCancelable(false)
        progressDialog?.dismiss()
    }

    protected fun showProgress() {
        hideKeyboard()
        progressDialog?.show()
    }

    protected fun hideProgress() {
        if (progressDialog?.isShowing == true)
            progressDialog?.dismiss()
    }

    protected fun hideKeyboard() {
        val view = activity?.currentFocus ?: return
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
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
        if (message == "401" || message == "403") {
            SessionService.token = ""
            val builder = AlertDialog.Builder(activity!!)
                    .setMessage(resources.getString(R.string.session_expired))
                    .setPositiveButton(R.string.ok) { _, _ ->
                        onSuccessLogout()
                    }
            builder.create().show()

            return
        }
        error(message)
    }

    protected open fun error(message: String, duration: Int = Snackbar.LENGTH_LONG) {
        errorMessage(message)
    }

    private fun errorMessage(message: String) {

        if (activity != null) {
            val builder = AlertDialog.Builder(activity!!)
                    .setMessage(message)
                    .setPositiveButton(R.string.ok, null)

            builder.create().show()
        }
    }

    private fun onSuccessLogout() {
        hideProgress()
        openLoginActivity()
    }

    private fun openLoginActivity() {
        SessionService.token = ""

        val intent = Intent(activity, LoginActivity::class.java)
        startActivity(intent)
        ActivityCompat.finishAffinity(activity!!)

    }

    protected fun getBold(value: String): SpannableStringBuilder{
        return SpannableStringBuilder()
            .bold { append(value) }
    }
}
