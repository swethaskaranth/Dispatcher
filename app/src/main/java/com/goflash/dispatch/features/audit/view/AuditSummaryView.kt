package com.goflash.dispatch.features.audit.view


interface AuditSummaryView{

    fun onSuccess()

    fun onFailure(error : Throwable?)

    fun refreshList()

    fun takeToSummaryScreen(auditId : Long, startTime : String, endTime : String)

    fun showAlert()

}