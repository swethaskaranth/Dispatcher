package com.goflash.dispatch.data

data class SprinterList (val id:String,
                         val name:String,
                         val assetId:String,
                         val assetName:String,
                         val contactNumber:String,
                         val phone : String,
                         val assignedZoneId:String?,
                         val assignedZoneName:String?,
                         val restricted: Boolean = false,
                         val restrictionReason: String?
)