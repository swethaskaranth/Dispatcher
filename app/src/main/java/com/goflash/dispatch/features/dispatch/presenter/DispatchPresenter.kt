package com.goflash.dispatch.features.dispatch.presenter

import android.content.Context
import com.goflash.dispatch.features.dispatch.view.DispatchView

interface DispatchPresenter{

    fun onAttachView(context: Context, view: DispatchView)

    fun onDetachView()

    fun initiateDispatch(barcode: String)

}