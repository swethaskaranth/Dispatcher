package com.goflash.dispatch.features.audit.view


interface AuditView{

    fun onSuccess(bagCount : Long, shipmentCount : Long, auditId : Long)

    fun onFailure(error : Throwable?)

    fun showActiveAuditData(userName : String, createdTime : String)
}