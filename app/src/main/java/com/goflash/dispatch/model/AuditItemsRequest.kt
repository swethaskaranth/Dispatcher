package com.goflash.dispatch.model

data class AuditItemsRequest(
    val auditId: Long,
    val completed: Boolean,
    val referenceId: List<String>
)