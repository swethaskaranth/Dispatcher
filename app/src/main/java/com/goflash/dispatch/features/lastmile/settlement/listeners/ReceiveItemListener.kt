package com.goflash.dispatch.features.lastmile.settlement.listeners

interface ReceiveItemListener {

    fun onAcceptorRejectMedicine(itemId : Int,ucode : String?, display : String, batch : String?, quantity : Int, reason : String, accept : Boolean, rejectRemarks: String)

}