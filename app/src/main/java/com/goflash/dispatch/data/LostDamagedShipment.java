package com.goflash.dispatch.data;

import android.os.Parcel;
import android.os.Parcelable;

import co.uk.rushorm.core.Rush;
import co.uk.rushorm.core.RushCallback;
import co.uk.rushorm.core.RushCore;

public class LostDamagedShipment implements Rush, Parcelable {

    private String shipmentId;
    private String referenceId;
    private String status;
    private String lbn;
    private String packageId;
    private String shipmentType;

    public String getShipmentId() {
        return shipmentId;
    }

    public void setShipmentId(String shipmentId) {
        this.shipmentId = shipmentId;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLbn() {
        return lbn;
    }

    public void setLbn(String lbn) {
        this.lbn = lbn;
    }

    public String getPackageId() {
        return packageId;
    }

    public void setPackageId(String packageId) {
        this.packageId = packageId;
    }

    public String getShipmentType() {
        return shipmentType;
    }

    public void setShipmentType(String shipmentType) {
        this.shipmentType = shipmentType;
    }

    @Override
    public void save() {
        RushCore.getInstance().save(this);
    }

    @Override
    public void save(RushCallback rushCallback) {
        RushCore.getInstance().save(this, rushCallback);
    }

    @Override
    public void delete() {
        RushCore.getInstance().delete(this);
    }

    @Override
    public void delete(RushCallback rushCallback) {
        RushCore.getInstance().delete(this, rushCallback);
    }

    @Override
    public String getId() {
        return RushCore.getInstance().getId(this);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(shipmentId);
        parcel.writeString(referenceId);
        parcel.writeString(status);
        parcel.writeString(lbn);
        parcel.writeString(packageId);
        parcel.writeString(shipmentType);
    }

    public LostDamagedShipment(Parcel in){
        shipmentId = in.readString();
        referenceId = in.readString();
        status = in.readString();
        lbn = in.readString();
        packageId = in.readString();
        shipmentType = in.readString();
    }

    public static final Creator<LostDamagedShipment> CREATOR = new Creator<LostDamagedShipment>() {
        @Override
        public LostDamagedShipment createFromParcel(Parcel in) {
            return new LostDamagedShipment(in);
        }

        @Override
        public LostDamagedShipment[] newArray(int size) {
            return new LostDamagedShipment[size];
        }
    };

    public static Creator<LostDamagedShipment> getCREATOR() {
        return CREATOR;
    }

    public LostDamagedShipment(){}

    public LostDamagedShipment(String shipmentId, String referenceId, String status, String lbn, String packageId, String shipmentType) {
        this.shipmentId = shipmentId;
        this.referenceId = referenceId;
        this.status = status;
        this.lbn = lbn;
        this.packageId = packageId;
        this.shipmentType = shipmentType;
    }
}
