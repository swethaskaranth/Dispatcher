package com.goflash.dispatch.model

import android.os.Parcel
import android.os.Parcelable

data class ZoneListDTO(
    var id: Int,
    var zoneName: String?,
    var shipmentCount: Int,
    var priorityCount: Int,
    var selected: Boolean = false,
    var tripCreationInProgress: Boolean = false,
    var tripProcessId: Int = 0,
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readInt(),
        (parcel.readInt() != 0),
        (parcel.readInt() != 0)
    ) {
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(id)
        dest.writeString(zoneName)
        dest.writeInt(shipmentCount)
        dest.writeInt(priorityCount)
        dest.writeInt(if(selected) 1 else 0)
        dest.writeInt(if(tripCreationInProgress) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ZoneListDTO> {
        override fun createFromParcel(parcel: Parcel): ZoneListDTO {
            return ZoneListDTO(parcel)
        }

        override fun newArray(size: Int): Array<ZoneListDTO?> {
            return arrayOfNulls(size)
        }
    }
}