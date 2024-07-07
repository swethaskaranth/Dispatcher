package com.goflash.dispatch.data

data class Profile(
        val token: String,
        val name: String,
        val userId: String,
        val roles: List<String>,
        val assignedAssetName : String,
        val assignedAssetId: Long,
        val invoiceGenerationFlag : Boolean,
        val email : String?,
        val selfAssignment: Boolean,
        val singleScanSortation: Boolean
)

data class Warehouse(
        val id: Int,
        val name: String,
        val tenant: String
)
