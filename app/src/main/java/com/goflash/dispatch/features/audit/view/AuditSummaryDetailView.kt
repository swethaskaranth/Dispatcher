package com.goflash.dispatch.features.audit.view

interface AuditSummaryDetailView {

    fun onFailure(error : Throwable?)

    fun setNameAndAsset(name: String?, asset : String?)

    fun setBagCount(scanned: Long, expected : Long)

    fun setShipmentCount(scanned: Long, expected : Long)

    fun setBagShortAndExtraCount(short : Long, extra : Long)

    fun setShipmentShortAndExtraCount(short : Long, extra : Long)

    fun setStartTime(time : String)

    fun setEndTime(time : String)
}