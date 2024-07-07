package com.goflash.dispatch.model

import com.goflash.dispatch.data.SprinterForZone

data class SprinterShipmentList(val sprinterList: MutableList<SprinterForZone>,
                                val shipmentIdList: MutableList<String>,
                                val zoneIds: MutableList<Long>?)
