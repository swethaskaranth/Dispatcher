package com.goflash.dispatch.data

data class BagTripSummaryDetail (val sortBagged:BagShipmentCount?,
                                 val dispatched:BagShipmentCount?,
                                 val received:BagShipmentCount?,
                                 val activeAudit : Boolean?
)