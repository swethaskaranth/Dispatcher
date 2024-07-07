package com.goflash.dispatch.features.lastmile.settlement.presenter.impl

import android.content.Context
import co.uk.rushorm.core.RushSearch
import com.goflash.dispatch.data.AckForRecon
import com.goflash.dispatch.data.AckSlipDto
import com.goflash.dispatch.features.lastmile.settlement.presenter.ApproveImagePresenter
import com.goflash.dispatch.features.lastmile.settlement.view.ApproveImageView
import com.goflash.dispatch.type.AckSource
import com.goflash.dispatch.type.AckStatus

class ApproveImagePresenterImpl() : ApproveImagePresenter {
    private var mView: ApproveImageView? = null

    private var ackForRecon: AckForRecon? = null
    private var ackSlips: MutableList<AckSlipDto> = mutableListOf()

    lateinit var selectedAckSlip: AckSlipDto

    override fun onAttach(context: Context, view: ApproveImageView) {
        this.mView = view
    }

    override fun onDetach() {
        if (mView == null)
            return
        mView = null
    }

    override fun getAckSlipsForLBN(tripId: Long, lbn: String) {
        ackSlips.clear()
        ackForRecon = RushSearch().whereEqual("lbn", lbn).findSingle(AckForRecon::class.java)

        ackSlips.addAll(ackForRecon!!.ackList.toMutableList())//.filter { it.source != AckSource.DISPATCHER.name && it.status != AckStatus.ACCEPTED.name })

        if (ackSlips.isEmpty())
            mView?.closeActivity()
        else
            mView?.onAckSlipsFetched(ackSlips)

    }

    override fun onPageSelected(position: Int) {
        if (position < ackSlips.size)
            selectedAckSlip = ackSlips[position]
    }

    override fun onApproveImageClicked() {
        selectedAckSlip.status = AckStatus.ACCEPTED.name
        selectedAckSlip.save()
        ackSlips.remove(selectedAckSlip)
        mView?.onAckSlipsFetched(ackSlips)
        mView?.onImageApproved("Image Approved")

    }

    override fun onRejectImageClicked() {
        selectedAckSlip.status = AckStatus.REJECTED.name
        selectedAckSlip.save()
        ackSlips.remove(selectedAckSlip)
        mView?.onAckSlipsFetched(ackSlips)
        mView?.onImageApproved("Image Rejected")
    }
}