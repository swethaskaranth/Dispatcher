package com.goflash.dispatch.model

data class AuditHistory(
    val id: Long,
    val createdOn: String,
    val updatedOn: String,
    val createdBy : String,
    val updatedBy : String,
    val assetId: Long,
    val assetName: String,
    val status: String,
    val userName : String
)