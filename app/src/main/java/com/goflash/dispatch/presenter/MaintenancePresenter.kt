package com.goflash.dispatch.presenter

import android.content.Context
import com.goflash.dispatch.presenter.views.MaintenanceView

interface MaintenancePresenter {

    fun onAttachView(context : Context, view : MaintenanceView)

    fun onDetachView()

    fun checkHealthStatus()
}