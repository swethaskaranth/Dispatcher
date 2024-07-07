package com.goflash.dispatch.data

import com.google.gson.annotations.SerializedName

data class ReceiveChildShipment(@SerializedName("shipment-id")
                                val shipmentId: Int?,
                                @SerializedName("shipment-type")
                                val shipmentType: String?,
                                val status: String?,
                                @SerializedName("way-bill-number")
                                val wayBillNumber: String?,
                                @SerializedName("reference-id")
                                val referenceId: String?,
                                val received: Boolean?,
                                @SerializedName("received-in-run-id")
                                val receivedInRunId: Int,
                                @SerializedName("received-status")
                                val receivedStatus: String?,
                                @SerializedName("return-state")
                                val returnState: String?,
                                @SerializedName("delivery-type")
                                val deliveryType: String?,
                                val lbn: String,
                                val courier: Boolean
                                )
