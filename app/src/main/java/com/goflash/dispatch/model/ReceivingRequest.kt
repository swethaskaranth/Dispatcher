package com.goflash.dispatch.model

import java.io.Serializable

/**
 * Created by Ravi on 2019-09-17.
 */
data class ReceivingRequest(var tripId: String? = null,
                       var vehicleSealId: String? = null,
                       var tripCompleted: Boolean? = null,
                       var bagAdded: MutableList<BagDetails> = mutableListOf(),
                       var bagRemoved: MutableList<BagDetails> = mutableListOf()
) : Serializable