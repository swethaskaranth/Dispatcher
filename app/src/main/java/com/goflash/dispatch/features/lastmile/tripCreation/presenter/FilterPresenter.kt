package com.goflash.dispatch.features.lastmile.tripCreation.presenter

import android.content.Context
import com.goflash.dispatch.features.lastmile.tripCreation.view.FilterView

interface FilterPresenter {

    fun onAttach(context: Context, view : FilterView)

    fun onDetach()

}