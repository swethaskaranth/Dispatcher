package com.goflash.dispatch.data;

import android.os.Parcel;
import android.os.Parcelable;
import co.uk.rushorm.core.Rush;
import co.uk.rushorm.core.RushCallback;
import co.uk.rushorm.core.RushCore;
import co.uk.rushorm.core.annotations.RushList;

import java.util.ArrayList;

public final class PackageDto implements Parcelable, Rush {

    @RushList(classType = ScannedOrder.class )
    private ArrayList<ScannedOrder> scannedOrders;

    private boolean furtherScannable;

    private long tripId;
    private boolean isSprinterBin;


    public  PackageDto(){

    }

    public PackageDto( ArrayList scannedOrders,boolean furtherScannable){
        this.scannedOrders = scannedOrders;

        this.furtherScannable = furtherScannable;
    }
    public PackageDto( ArrayList scannedOrders,boolean furtherScannable, long tripId, boolean isSprinterBin){
        this.scannedOrders = scannedOrders;

        this.furtherScannable = furtherScannable;
        this.tripId = tripId;
        this.isSprinterBin = isSprinterBin;
    }

    protected PackageDto(Parcel in) {
        scannedOrders = in.createTypedArrayList(ScannedOrder.CREATOR);
        furtherScannable = in.readByte() != 0;
        tripId = in.readLong();
        isSprinterBin = in.readByte() != 0;
    }

    public static final Creator<PackageDto> CREATOR = new Creator<PackageDto>() {
        @Override
        public PackageDto createFromParcel(Parcel in) {
            return new PackageDto(in);
        }

        @Override
        public PackageDto[] newArray(int size) {
            return new PackageDto[size];
        }
    };

    public ArrayList<ScannedOrder> getScannedOrders() {
        return scannedOrders;
    }

    public void setScannedOrders(ArrayList<ScannedOrder> scannedOrders) {
        this.scannedOrders = scannedOrders;
    }

    public boolean isFurtherScannable() {
        return furtherScannable;
    }

    public void setFurtherScannable(boolean furtherScannable) {
        this.furtherScannable = furtherScannable;
    }

    public long getTripId() {
        return tripId;
    }

    public void setTripId(long tripId) {
        this.tripId = tripId;
    }

    public boolean getSprinterBin() {
        return isSprinterBin;
    }

    public void setSprinterBin(boolean sprinterBin) {
        isSprinterBin = sprinterBin;
    }

    public String toString() {
        return "PackageDto(scannedOrders=" + this.scannedOrders +  ", furtherScannable=" + this.furtherScannable + ")";
    }

    public void save() {
        RushCore.getInstance().saveOnlyWithoutConflict(this);
    }

    public void save( RushCallback callback) {
        RushCore.getInstance().save(this, callback);
    }

    public void delete() {
        RushCore.getInstance().delete((Rush)this);
    }

    public void delete( RushCallback callback) {
        RushCore.getInstance().delete(this, callback);
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
        parcel.writeTypedList(scannedOrders);
        parcel.writeByte((byte) (furtherScannable ? 1 : 0));
        parcel.writeLong(tripId);
        parcel.writeByte((byte) (isSprinterBin ? 1 : 0));
    }
}
