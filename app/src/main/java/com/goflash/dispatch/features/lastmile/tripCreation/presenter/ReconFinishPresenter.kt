package com.goflash.dispatch.features.lastmile.tripCreation.presenter

import android.content.Context
import com.goflash.dispatch.features.lastmile.tripCreation.view.ReconFinishView

interface ReconFinishPresenter{

    fun onAttachView(context: Context, view: ReconFinishView)

    fun onDetachView()

    fun getReconFinishTrips()

}