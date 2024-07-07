package com.goflash.dispatch.data

data class InwardRunResponse(
    val completed: List<InwardRun>?,
    val pending: InwardRun?
)
