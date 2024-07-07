package com.goflash.dispatch.model

data class AuditSummary(
    val auditId: Long,
    val assetId: Long,
    val assetName: String,
    val userId: String,
    val userName: String,
    val bagCount : BagCount,
    val shipmentCount : BagCount
)