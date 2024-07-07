package com.goflash.dispatch.data

data class PartnerNameDTO(
    val name: String,
    val count: Int,
    var selected: Boolean = false
)