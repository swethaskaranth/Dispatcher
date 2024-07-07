package com.goflash.dispatch.features.receiving.presenter

import android.content.Context
import com.goflash.dispatch.features.receiving.view.ReceivingView

/**
 *Created by Ravi on 2019-09-08.
 */
interface ReceivingPresenter {

    fun onAttachView(context: Context, view: ReceivingView)

    fun onDetachView()

    fun getAllTasks()

    fun searchByVehicleSeal(str: String)

    fun clearFilter()
}