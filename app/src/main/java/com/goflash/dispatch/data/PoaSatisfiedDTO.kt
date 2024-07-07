package com.goflash.dispatch.data

import com.goflash.dispatch.type.PoaType

data class PoaSatisfiedDTO(
    val poa: PoaType,
    val isSatisfied: Boolean
)
