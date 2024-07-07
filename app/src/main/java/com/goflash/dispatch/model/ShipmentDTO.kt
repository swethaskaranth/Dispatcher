package com.goflash.dispatch.model

data class ShipmentDTO(

    var shipmentId: String,
    var referenceId: String?,
    var orderId: String?,
    var assetName: String?,
    var name: String?,
    var address1: String?,
    var address2: String?,
    var address3: String?,
    var city: String?,
    var state: String?,
    var pincode: String?,
    var contactNumber: String?,
    var addressType: String?,
    var latitude: String?,
    var longitude: String?,
    var type: String?,
    var lbn: String?,
    var committedExpectedDeliveryDate: String? = "",
    var priorityType: String?,
    var packageId: String?,
    var status: String?,
    var postponedToDate: String?,
    var processingBlocked: Boolean = false,
    var consumerType: String? = null,
    var shipmentType: String? = null,
    var parentShipment: String? = null,
    var mpsCount: Int,
    var selected: Boolean = false
)

