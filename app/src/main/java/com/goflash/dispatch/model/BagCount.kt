package com.goflash.dispatch.model

data class BagCount(
    val expectedCount: Long,
    val scannedCount: Long,
    val extraCount: Long,
    val missedCount: Long
)