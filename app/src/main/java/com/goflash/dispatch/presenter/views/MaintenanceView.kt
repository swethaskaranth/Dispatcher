package com.goflash.dispatch.presenter.views

interface MaintenanceView {

    fun onMaintenanceModeFetched(on: Boolean)

    fun onFailure(error: Throwable?)
}