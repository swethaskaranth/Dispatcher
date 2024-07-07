package com.goflash.dispatch.presenter.impl

import android.content.Context
import android.content.Intent
import android.util.Log
import com.goflash.dispatch.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.goflash.dispatch.api_services.SessionService
import com.goflash.dispatch.data.Profile
import com.goflash.dispatch.di.interactor.SortationApiInteractor
import com.goflash.dispatch.presenter.LoginPresenter
import com.goflash.dispatch.presenter.views.LoginView
import com.goflash.dispatch.model.Credentials
import com.goflash.dispatch.util.PreferenceHelper
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription

/**
 *Created by Ravi on 28/05/19.
 */
class LoginPresenterImpl(private val sortationApiInteractor: SortationApiInteractor) :
    LoginPresenter {


    private val TAG = LoginPresenterImpl::class.java.simpleName

    private var loginView: LoginView? = null

    private var compositeSubscription: CompositeSubscription? = null

    //private lateinit var auth: FirebaseAuth
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    override fun onAttachView(context: Context, view: LoginView) {
        this.loginView = view
        compositeSubscription = CompositeSubscription()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.server_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(context, gso)
    }

    override fun onDetachView() {
        if (this.loginView == null)
            return
        loginView = null
        compositeSubscription?.unsubscribe()
        compositeSubscription = null
    }

    override fun callLoginAPi(idToken: String?) {
        compositeSubscription?.add(sortationApiInteractor.login(
            Credentials(
                idToken
            )
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ profile ->
                setUserData(profile)
                loginView?.onSuccess(profile)
            }, { error: Throwable? ->
                signout()
                loginView?.onFailure(error)
            })
        )
    }

    private fun setUserData(profile: Profile) {
        SessionService.token = profile.token
        PreferenceHelper.token = profile.token
        SessionService.userId = profile.userId
        SessionService.name = profile.name
        PreferenceHelper.assignedAssetName = profile.assignedAssetName
        PreferenceHelper.assignedAssetId = profile.assignedAssetId
        PreferenceHelper.invoiceGenerationFlag = profile.invoiceGenerationFlag
        SessionService.email = profile.email
        SessionService.roles.addAll(profile.roles)
        SessionService.selfAssignment = profile.selfAssignment
        PreferenceHelper.singleScanSortation = profile.singleScanSortation
    }

    override fun signIn(context: Context) {

        val signInIntent = mGoogleSignInClient.signInIntent
        loginView?.startGoogleLoginActivity(signInIntent)
    }

    override fun handleGoogleSigninIntent(intent: Intent?) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(intent)
        try {
            // Google Sign In was successful, authenticate with Firebase
            val account = task.getResult(ApiException::class.java)
            firebaseAuthWithGoogle(account!!)
        } catch (e: ApiException) {
            // Google Sign In failed, update UI appropriately
            Log.w(TAG, "Google sign in failed", e)
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        loginView?.updateUI(acct.idToken)
    }

    override fun signout() {
        mGoogleSignInClient.signOut()
    }

    override fun numberLogin(mobileNumber: String?, resendOtp: Boolean) {
        compositeSubscription?.add(sortationApiInteractor.getOtpLogin(
            Credentials(
                mobileNumber = mobileNumber,
                resendOtp = resendOtp
            )
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                loginView?.onSuccessOtp()


            }, { error ->
                signout()
                loginView?.onFailure(error)
            })
        )
    }

    override fun otpEnter(mobileNumber: String?, otp: String?) {
        compositeSubscription?.add(sortationApiInteractor.otpLogin(
            Credentials(
                mobileNumber = mobileNumber,
                otp = otp
            )
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ profile ->
                setUserData(profile)
                loginView?.onSuccess(profile)

            }, { error ->
                signout()
                loginView?.onFailure(error)
            })
        )
    }

}