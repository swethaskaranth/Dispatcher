package com.goflash.dispatch.data

import com.google.gson.annotations.SerializedName

data class CompleteInwardRunRequest(val status: String,
                                    val updatedBy: String,
                                    val updatedByName: String,
                                    val assetId: Int
)
