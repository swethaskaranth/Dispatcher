package com.goflash.dispatch.features.bagging.presenter

import android.content.Context
import com.goflash.dispatch.features.bagging.view.SortationView

interface SortPresenter{

    fun onAttachView(context: Context, view: SortationView)

    fun onDetachView()

    fun onBinScan(barCode: String)

    fun reInitialize()
}