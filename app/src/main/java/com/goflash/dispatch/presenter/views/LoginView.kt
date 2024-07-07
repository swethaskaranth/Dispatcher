package com.goflash.dispatch.presenter.views

import android.content.Intent
import com.goflash.dispatch.data.Profile

/**
 *Created by Ravi on 28/05/19.
 */
interface LoginView {

    /**
     * @param profile  [ callLoginAPi success profile result]
     * */
    fun onSuccess(profile: Profile)

    fun startGoogleLoginActivity(intent: Intent)

    fun updateUI(idToken: String?)

    fun onFailure(error : Throwable?)

    fun onSuccessOtp()

    fun showInvalidOtp()

}