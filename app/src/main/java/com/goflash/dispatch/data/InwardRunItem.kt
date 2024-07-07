package com.goflash.dispatch.data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class InwardRunItem(
 val id: Int,
 val lbn: String,
 val exceptionRaised: Boolean,
 val multipartShipment: Boolean,
 val inwardRunId: Int,
 val createdBy: String? = null,
 val createdByName: String? = null,
 val createdOn: String? = null,
 val deliveryType: String? = null,
 val exceptionReasons: List<String?>? = null,
 val imageUrl: String? = null,
 val mpsParentId: Int? = null,
 val partnerName: String? = null,
 val referenceId: String? = null,
 val returnType: String? = null,
 val shipmentId: Int? = null,
 val shipmentStatus: String? = null,
 val shipmentType: String? = null,
 var status: String? = null,
 val updatedBy: String? = null,
 val updatedByName: String? = null,
 val updatedOn: String? = null,
 val wayBillNumber: String? = null,
 val mpsParentLbn: String? = null,
 var mpsScannedCount: Int = -1,
 var mpsCount: Int = -1,
 var scannedBarcode: String? = null,
 var rejectFlowRequired: Boolean = true

): Parcelable