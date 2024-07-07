package com.goflash.dispatch.features.lastmile.settlement.listeners

import com.goflash.dispatch.type.ReconStatus

interface ReviewItemListener {

    fun onAcceptOrRejectSelected(position : Int,reconStatus : ReconStatus, reconReason : String, rejectRemarks: String)

}