package com.goflash.dispatch.data;

import android.os.Parcel;
import android.os.Parcelable;
import co.uk.rushorm.core.Rush;
import co.uk.rushorm.core.RushCallback;
import co.uk.rushorm.core.RushCore;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Ravi on 2019-09-16.
 */
public class Tasks implements Parcelable, Rush {

    @SerializedName("id")
    private String tripId;

    private String status;

    private String shipmentId;

    protected Tasks(Parcel in) {
        tripId = in.readString();
        status = in.readString();
        shipmentId = in.readString();
    }

    public static final Creator<Tasks> CREATOR = new Creator<Tasks>() {
        @Override
        public Tasks createFromParcel(Parcel in) {
            return new Tasks(in);
        }

        @Override
        public Tasks[] newArray(int size) {
            return new Tasks[size];
        }
    };

    public void save() {
        RushCore.getInstance().saveOnlyWithoutConflict(this);
    }

    public void save( RushCallback callback) {
        RushCore.getInstance().save(this, callback);
    }

    public void delete() {
        RushCore.getInstance().delete(this);
    }

    public void delete( RushCallback callback) {
        RushCore.getInstance().delete(this, callback);
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getShipmentId() {
        return shipmentId;
    }

    public void setShipmentId(String shipmentId) {
        this.shipmentId = shipmentId;
    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(tripId);
        parcel.writeString(status);
        parcel.writeString(shipmentId);
    }
}
