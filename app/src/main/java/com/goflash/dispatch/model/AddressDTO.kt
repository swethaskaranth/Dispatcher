package com.goflash.dispatch.model

import android.os.Parcel
import android.os.Parcelable

data class AddressDTO(
    var name: String?,
    var address1: String?,
    var address2: String?,
    var address3: String?,
    var city: String?,
    var state: String?,
    var pincode: String?,
    var contactNumber: String?,
    var addressType: String?,
    var latitude: String?,
    var longitude: String?
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(address1)
        parcel.writeString(address2)
        parcel.writeString(address3)
        parcel.writeString(city)
        parcel.writeString(state)
        parcel.writeString(pincode)
        parcel.writeString(contactNumber)
        parcel.writeString(addressType)
        parcel.writeString(latitude)
        parcel.writeString(longitude)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun toString(): String {
        return "AddressDTO(name=$name, address1=$address1, address2=$address2, address3=$address3, city=$city, state=$state, pincode=$pincode, contactNumber=$contactNumber, addressType=$addressType, latitude=$latitude, longitude=$longitude)"
    }

    companion object CREATOR : Parcelable.Creator<AddressDTO> {
        override fun createFromParcel(parcel: Parcel): AddressDTO {
            return AddressDTO(parcel)
        }

        override fun newArray(size: Int): Array<AddressDTO?> {
            return arrayOfNulls(size)
        }
    }
}