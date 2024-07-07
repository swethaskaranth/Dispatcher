package com.goflash.dispatch.data;

import android.os.Parcel;
import android.os.Parcelable;

import org.jetbrains.annotations.NotNull;

import co.uk.rushorm.core.Rush;
import co.uk.rushorm.core.RushCallback;
import co.uk.rushorm.core.RushCore;

public class FmPickedShipment implements Parcelable, Rush {

    String shipmentId;
    String referenceId;
    String lbn;
    Integer originAddressId;
    String originName;
    String packageId;
    String tag;
    Long tripId;
    boolean scanned;
    String reason;

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

    public String getLbn() {
        return lbn;
    }

    public void setLbn(String lbn) {
        this.lbn = lbn;
    }

    public Integer getOriginAddressId() {
        return originAddressId;
    }

    public void setOriginAddressId(Integer originAddressId) {
        this.originAddressId = originAddressId;
    }

    public String getOriginName() {
        return originName;
    }

    public void setOriginName(String originName) {
        this.originName = originName;
    }

    public String getPackageId() {
        return packageId;
    }

    public void setPackageId(String packageId) {
        this.packageId = packageId;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public Long getTripId() {
        return tripId;
    }

    public void setTripId(Long tripId) {
        this.tripId = tripId;
    }

    public boolean isScanned() {
        return scanned;
    }

    public void setScanned(boolean scanned) {
        this.scanned = scanned;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public FmPickedShipment() {
    }

    protected FmPickedShipment(Parcel in){
        shipmentId = in.readString();
        referenceId = in.readString();
        lbn = in.readString();
        originAddressId = in.readInt();
        originName = in.readString();
        packageId = in.readString();
        tag = in.readString();
        tripId = in.readLong();
        scanned = in.readByte() != 0;
        reason = in.readString();
    }

    public FmPickedShipment(String shipmentId, String referenceId, String lbn, Integer originAddressId, String originName, String packageId, String tag, Long tripId, String reason) {
        this.shipmentId = shipmentId;
        this.referenceId = referenceId;
        this.lbn = lbn;
        this.originAddressId = originAddressId;
        this.originName = originName;
        this.packageId = packageId;
        this.tag = tag;
        this.tripId = tripId;
        this.reason = reason;
    }

    public static final Creator<FmPickedShipment> CREATOR = new Creator<FmPickedShipment>() {
        @Override
        public FmPickedShipment createFromParcel(Parcel in) {
            return new FmPickedShipment(in);
        }

        @Override
        public FmPickedShipment[] newArray(int size) {
            return new FmPickedShipment[size];
        }
    };

    public static Creator<FmPickedShipment> getCREATOR() {
        return CREATOR;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel in, int i) {
        in.writeString(shipmentId);
        in.writeString(referenceId);
        in.writeString(lbn);
        in.writeInt(originAddressId);
        in.writeString(originName);
        in.writeString(packageId);
        in.writeString(tag);
        in.writeLong(tripId);
        in.writeByte((byte) (scanned?1:0));
        in.writeString(reason);
    }

    public void save() {
        RushCore.getInstance().saveOnlyWithoutConflict(this);
    }

    public void save(@NotNull RushCallback callback) {
        RushCore.getInstance().save(this, callback);
    }

    public void delete() {
        RushCore.getInstance().delete(this);
    }

    public void delete(@NotNull RushCallback callback) {
        RushCore.getInstance().delete(this, callback);
    }

    @NotNull
    public String getId() {
        return RushCore.getInstance().getId(this);
    }

}
