package com.goflash.dispatch.presenter.views

import com.goflash.dispatch.data.Profile

interface UserView {

    fun onFailure(error: Throwable?)

    fun onSuccess(userProfile: Profile)

    fun setViews(name : String)

    fun onSuccessLogout()

}