package com.goflash.dispatch.presenter

import android.app.Activity
import android.content.Context
import com.goflash.dispatch.presenter.views.MainView

interface MainPrsenter {

    fun onAttachView(context : Context, view : MainView)

    fun onDetachView()

    fun checkIfDispatchStarted()

    fun onLogout(activity : Activity, server_client_id : String)



}