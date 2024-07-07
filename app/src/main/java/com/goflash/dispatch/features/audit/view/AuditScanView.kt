package com.goflash.dispatch.features.audit.view


interface AuditScanView{

    fun onSuccess()

    fun onFailure(error : Throwable?)

    fun showAuditInactive()

    fun showCount(bagCount : Long, shipmentCount : Long)

}