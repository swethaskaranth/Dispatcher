package com.goflash.dispatch.features.lastmile.settlement.view

import com.goflash.dispatch.data.FmPickedShipment

interface FmPickupReviewView {

    fun onShipmentsFetched(list: List<FmPickedShipment>)
}