package com.goflash.dispatch.features.lastmile.settlement.presenter

import android.content.Context
import com.goflash.dispatch.features.lastmile.settlement.view.ApproveImageView

interface ApproveImagePresenter {

    fun onAttach(context: Context, view: ApproveImageView)

    fun onDetach()

    fun getAckSlipsForLBN(tripId: Long, lbn: String)

    fun onPageSelected(position: Int)

    fun onApproveImageClicked()

    fun onRejectImageClicked()
}