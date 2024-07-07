package com.goflash.dispatch.data

import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable

data class ChildShipmentDTO (val shipmentId: String,
                             val lbn: String?,
                             val referenceId: String?,
                             val packageId: String?,
                             val parentShipment: String?,
                             val status:String?
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()?:"",
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(shipmentId)
        parcel.writeString(lbn)
        parcel.writeString(referenceId)
        parcel.writeString(packageId)
        parcel.writeString(parentShipment)
        parcel.writeString(status)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ChildShipmentDTO> {
        override fun createFromParcel(parcel: Parcel): ChildShipmentDTO {
            return ChildShipmentDTO(parcel)
        }

        override fun newArray(size: Int): Array<ChildShipmentDTO?> {
            return arrayOfNulls(size)
        }
    }
}