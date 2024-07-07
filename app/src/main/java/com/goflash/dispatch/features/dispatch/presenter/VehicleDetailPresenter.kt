package com.goflash.dispatch.features.dispatch.presenter

import android.content.Context
import android.content.Intent
import com.goflash.dispatch.features.dispatch.view.VehicleDetailView

interface VehicleDetailPresenter {

    fun onAttachView(context: Context, view : VehicleDetailView)

    fun sendIntent(intent : Intent)

    fun onDetachView()

    fun getSprinterList()

    fun onSprinterSelected(sprinter : String)

    fun getSelectedSprinter()

    fun onProceedClicked(transportMode: String?)

    fun validateVehicleNumber(number: String)

    fun getSelectedSprinterDetails()
}