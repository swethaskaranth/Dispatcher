package com.goflash.dispatch.model

import com.goflash.dispatch.data.SprinterForZone

data class ZoneSprinterDTO(
    val sprinterList: MutableList<SprinterForZone>,
    val zoneList: MutableList<ZoneListDTO>
)