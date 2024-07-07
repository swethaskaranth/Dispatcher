package com.goflash.dispatch.features.dispatch.presenter

import android.content.Context
import com.goflash.dispatch.features.dispatch.view.RunsheetListView

interface RunsheetListPresenter {

    fun onAttachView(context: Context, view: RunsheetListView)

    fun onDetachView()

    fun getRunsheetList()
}