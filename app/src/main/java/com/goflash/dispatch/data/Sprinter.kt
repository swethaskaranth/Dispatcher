package com.goflash.dispatch.data

import android.os.Parcel
import android.os.Parcelable

data class Sprinter(val id: String,
                    val name: String,
                    val contactNumber: String,
                    var vehicleNumber: String?) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(contactNumber)
        parcel.writeString(vehicleNumber)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Sprinter> {
        override fun createFromParcel(parcel: Parcel): Sprinter {
            return Sprinter(parcel)
        }

        override fun newArray(size: Int): Array<Sprinter?> {
            return arrayOfNulls(size)
        }
    }

}