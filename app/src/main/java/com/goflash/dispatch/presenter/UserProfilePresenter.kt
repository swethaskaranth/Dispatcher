package com.goflash.dispatch.presenter

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.work.WorkInfo
import com.goflash.dispatch.presenter.views.UserView

interface UserProfilePresenter {

    fun onAttachView(context: Context, userView: UserView)

    fun onDetachView()

    fun getUserDetails()

    fun logout()

}