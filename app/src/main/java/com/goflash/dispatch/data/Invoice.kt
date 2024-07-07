package com.goflash.dispatch.data

data class Invoice (val id : Long,
                    val invoiceId: String,
                    val tripId : String,
                    val status : String,
                    val invoiceUrl : String?,
                    val wayWillNumberUrl : String?,
                    val createdOn : String,
                    val vehicleId : String)