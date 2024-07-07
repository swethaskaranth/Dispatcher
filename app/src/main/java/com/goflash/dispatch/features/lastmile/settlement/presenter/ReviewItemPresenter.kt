package com.goflash.dispatch.features.lastmile.settlement.presenter

import android.content.Context
import android.content.Intent
import com.goflash.dispatch.features.lastmile.settlement.view.ReviewItemView
import com.goflash.dispatch.type.ReconStatus

interface ReviewItemPresenter {

    fun onAttachView(context: Context, view : ReviewItemView)

    fun onDetachView()

    fun sendIntent(intent : Intent?)

    fun setAcceptReject(position : Int, reconStatus: ReconStatus, reason : String, rejectRemarks: String)
}