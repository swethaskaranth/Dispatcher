package com.goflash.dispatch.features.rtoReceiving.presenter

import android.content.Context
import android.content.Intent
import com.goflash.dispatch.features.rtoReceiving.view.RunsheetView

interface RunsheetPresenter {

    fun onAttach(context: Context, view: RunsheetView)

    fun onDetach()

    fun sendIntent(intent: Intent)

    fun getRunsheetUrl()

    fun getMpsRunItems(position: Int)

}