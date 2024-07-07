package com.goflash.dispatch.ui.activity

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import co.uk.rushorm.core.RushCore
import com.goflash.dispatch.R
import com.goflash.dispatch.SortationApplication
import com.goflash.dispatch.api_services.SessionService
import com.goflash.dispatch.data.Profile
import com.goflash.dispatch.databinding.ActivityUserProfileBinding
import com.goflash.dispatch.di.components.DaggerActivityComponents
import com.goflash.dispatch.presenter.UserProfilePresenter
import com.goflash.dispatch.presenter.views.UserView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import javax.inject.Inject


class ProfileActivity : BaseActivity(), View.OnClickListener, UserView {

    @Inject
    lateinit var userProfilePresenter: UserProfilePresenter

    private lateinit var mGoogleSignInClient: GoogleSignInClient

    lateinit var dialog: Dialog
    lateinit var progressbar: ProgressBar
    lateinit var progressText: TextView

    private lateinit var binding: ActivityUserProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolBar.iVProfileHome.setOnClickListener(this)
        binding.userDetails.include7.logout.setOnClickListener(this)

        initDagger()
        initViews()

        dialog = Dialog(this)

        binding.userDetails.version.text = """Version ${com.goflash.dispatch.BuildConfig.VERSION_NAME}"""

    }

    private fun initViews() {

        showProgress()
        binding.toolBar.toolbarTitle.text = getString(R.string.my_profile)
        userProfilePresenter.getUserDetails()


    }


    private fun initDagger() {
        DaggerActivityComponents.builder().networkComponent((application as SortationApplication).getNetworkComponent())
                .build().inject(this@ProfileActivity)

        userProfilePresenter.onAttachView(this@ProfileActivity, this)
    }

    override fun onClick(view: View?) {

        when (view!!.id) {
            R.id.iVProfileHome -> finish()

            R.id.logout -> {
                showProgress()
                logUserOut()
            }

        }
    }

    override fun onSuccess(userProfile: Profile) {

        hideProgress()

    }

    override fun onFailure(error: Throwable?) {
        hideProgress()

        if (error != null)
            processError(error)
    }

    override fun setViews(name: String) {
        hideProgress()
        binding.userDetails.include6.txtUserName.text = SessionService.name
        //txtMobileNo.text = userProfile.mobile
    }

    override fun onDestroy() {
        super.onDestroy()
        userProfilePresenter.onDetachView()
    }

    private fun logUserOut() {
        showProgress()
        userProfilePresenter.logout()

    }

    override fun onSuccessLogout() {
        hideProgress()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .requestEmail()
                .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        SessionService.token = ""

        mGoogleSignInClient.signOut().addOnCompleteListener {
            RushCore.getInstance().clearDatabase()
            openLoginActivity()
        }

    }

    private fun openLoginActivity() {
        SessionService.token = ""
        SessionService.roles.clear()

        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finishAffinity()

    }

}