package com.goflash.dispatch.ui.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.goflash.dispatch.R
import com.goflash.dispatch.SortationApplication
import com.goflash.dispatch.app_constants.RC_SIGN_IN
import com.goflash.dispatch.data.Profile
import com.goflash.dispatch.databinding.ActivityLoginBinding
import com.goflash.dispatch.di.components.DaggerActivityComponents
import com.goflash.dispatch.presenter.LoginPresenter
import com.goflash.dispatch.presenter.views.LoginView
import com.goflash.dispatch.util.PreferenceHelper
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.GoogleApiClient
import com.mukesh.OnOtpCompletionListener
import javax.inject.Inject


class LoginActivity : BaseActivity(), LoginView, View.OnClickListener,
    GoogleApiClient.OnConnectionFailedListener {

    private val TAG = LoginActivity::class.java.simpleName
    private val storage_request = 100

    @Inject
    lateinit var presenter: LoginPresenter

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkPermissions()
        initDagger()

        initViews()
    }

    private fun initViews() {
        val signInButton = findViewById<SignInButton>(R.id.sign_in_button)
        signInButton.setOnClickListener(this)
        signInButton.setSize(SignInButton.SIZE_STANDARD)
        customizeGooglePlusButton(signInButton)

        binding.loginContent.btnGetOtp.setOnClickListener(this)
        binding.loginContent.btnResend.setOnClickListener(this)
        binding.loginContent.ivBack.setOnClickListener(this)

        binding.loginContent.etMobile.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                if (s?.length == 10 && (s.toString().matches(Regex("[6789].*")))) {
                    hideKeyboard()
                    binding.loginContent.tvError.visibility = View.GONE
                    binding.loginContent.btnGetOtp.isEnabled = true
                    binding.loginContent.btnGetOtp.setBackgroundResource(R.drawable.get_otp_background_enabled)
                } else {
                    binding.loginContent.tvError.visibility = View.VISIBLE
                    binding.loginContent.btnGetOtp.isEnabled = false
                    binding.loginContent.btnGetOtp.setBackgroundResource(R.drawable.get_otp_background_disabled)
                }
            }
        })
    }


    private fun initDagger() {
        DaggerActivityComponents.builder()
            .networkComponent((application as SortationApplication).getNetworkComponent())
            .build().inject(this@LoginActivity)

        presenter.onAttachView(this@LoginActivity, this)
    }


    override fun onClick(view: View?) {

        when (view!!.id) {
            R.id.ivBack -> setupNumberView()
            R.id.btnGetOtp -> {
                PreferenceHelper.mobileNumber = binding.loginContent.etMobile.text.toString()
                showProgress()
                presenter.numberLogin(PreferenceHelper.mobileNumber, false)
            }
            R.id.btnResend -> {
                showProgress()
                presenter.numberLogin(PreferenceHelper.mobileNumber, true)
            }
            R.id.sign_in_button -> {

                showProgress()
                presenter.signIn(this@LoginActivity)

            }
        }
    }

    private fun setupNumberView(){
        binding.loginContent.clEnterMobile.visibility = View.VISIBLE
        binding.loginContent.clOtp.visibility = View.GONE
        binding.loginContent.tvError.visibility = View.GONE
    }

    override fun onSuccessOtp() {
        hideProgress()
        binding.loginContent.clEnterMobile.visibility = View.GONE
        binding.loginContent.tvLoginLabel.visibility = View.GONE
        binding.loginContent.clOtp.visibility = View.VISIBLE

        binding.loginContent.otpView.setOtpCompletionListener(object : OnOtpCompletionListener {
            override fun onOtpCompleted(otp: String) {
                showProgress()
                presenter.otpEnter(PreferenceHelper.mobileNumber, binding.loginContent.otpView?.text.toString())
            }
        })

        binding.loginContent.otpView.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })


        startTimer()
    }

    private fun startTimer() {

        binding.loginContent.btnResend.isEnabled = false
        binding.loginContent.btnResend.setBackgroundResource(R.drawable.get_otp_background_disabled)

        val countDownTimer = object : CountDownTimer((30 * 1000).toLong(), 100) {

            override fun onTick(millisUntilFinished: Long) {
                binding.loginContent.btnResend.text = String.format(getString(R.string.request_otp), millisUntilFinished / 1000)

            }

            override fun onFinish() {
                binding.loginContent.btnResend.isEnabled = true
                binding.loginContent.btnResend.setBackgroundResource(R.drawable.get_otp_background_enabled)
                binding.loginContent.btnResend.text = getString(R.string.resend_otp)
            }
        }

        countDownTimer.start()
    }

    override fun showInvalidOtp() {
        binding.loginContent.tvMessage.visibility = View.VISIBLE
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        presenter.handleGoogleSigninIntent(data)

    }

    override fun updateUI(idToken: String?) {

        if (idToken == null) {
            showToast(this, resources.getString(R.string.login_failed))
            return
        }
        presenter.callLoginAPi(idToken)
    }

    override fun onSuccess(profile: Profile) {
        hideProgress()
        showMainActivity()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDetachView()

    }

    override fun startGoogleLoginActivity(intent: Intent) {
        startActivityForResult(intent, RC_SIGN_IN)
    }

    private fun showMainActivity() {
        hideProgress()
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onConnectionFailed(p0: ConnectionResult) {

    }


    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                storage_request
            )
        }
    }

    override fun onFailure(error: Throwable?) {
        hideProgress()
        processError(error)
        // showMainActivity()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            storage_request -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return
            }
            else -> {
                // Ignore all other requests.
            }
        }
    }

    fun customizeGooglePlusButton(signInButton: SignInButton) {
        for (i in 0 until signInButton.childCount) {
            val v = signInButton.getChildAt(i)
            if (v is TextView) {
                val tv = v
                tv.text = "Login with google"
                return
            }
        }
    }


}