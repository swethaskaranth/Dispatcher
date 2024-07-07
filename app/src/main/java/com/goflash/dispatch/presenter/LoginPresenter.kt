package com.goflash.dispatch.presenter

import android.content.Context
import android.content.Intent
import com.goflash.dispatch.presenter.views.LoginView

/**
 *Created by Ravi on 28/05/19.
 */
interface LoginPresenter {

    fun onAttachView(context: Context, view: LoginView)

    fun onDetachView()

    /**
     * @param idToken [ google token after callLoginAPi]
     * */
    fun callLoginAPi(idToken: String?)

    fun handleGoogleSigninIntent(intent: Intent?)

    fun signIn(context: Context)

    fun signout()

    /**
     * @param mobileNumber [enter number at the time login]
     * */
    //fun login(idToken: String?)
    fun numberLogin(mobileNumber: String?, resendOtp : Boolean)

    /**
     * @param otp [ enter otp after success]
     * */
    fun otpEnter(mobileNumber: String?, otp: String?)
}