package com.goflash.dispatch.data

import com.google.gson.annotations.SerializedName

data class InwardRun(
    val assetId: Int,
    val createdBy: String?,
    val createdByName: String?,
    val createdOn: String?,
    val id: Int,
    val partnerId: Int,
    val partnerName: String?,
    val runsheetUrl: String?,
    val scannedItemCount: Int,
    val status: String?,
    val inwardRunItems: List<InwardRunItem>
)
