package com.goflash.dispatch.data;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import co.uk.rushorm.core.Rush;
import co.uk.rushorm.core.RushCallback;
import co.uk.rushorm.core.RushCore;

public class PoaResponseForRecon implements Parcelable, Rush {
    private long shipmentId;
    private long taskId;
    private String referenceId;
    private String lbn;
    private String packageId;
    private PoaResponse poaResponse;

    private boolean scanned;

    private boolean mandatory;

    private String cashReceiptBarcode;

    public PoaResponseForRecon(){

    }

    public PoaResponseForRecon(long shipmentId, long taskId, String referenceId, String lbn, String packageId, PoaResponse poaResponse, boolean scanned, boolean mandatory, String cashReceiptBarcode) {
        this.shipmentId = shipmentId;
        this.taskId = taskId;
        this.referenceId = referenceId;
        this.lbn = lbn;
        this.packageId = packageId;
        this.poaResponse = poaResponse;
        this.scanned = scanned;
        this.mandatory = mandatory;
        this.cashReceiptBarcode = cashReceiptBarcode;
    }

    public PoaResponseForRecon(Parcel in){
        this.shipmentId = in.readLong();
        this.taskId = in.readLong();
        this.referenceId = in.readString();
        this.lbn = in.readString();
        this.packageId = in.readString();
        this.poaResponse = in.readParcelable(PoaResponse.class.getClassLoader());
        this.scanned = in.readByte() != 0;
        this.mandatory = in.readByte() != 0;
        this.cashReceiptBarcode = in.readString();
    }

    public static final Creator<PoaResponseForRecon> CREATOR = new Creator<PoaResponseForRecon>() {
        @Override
        public PoaResponseForRecon createFromParcel(Parcel in) {
            return new PoaResponseForRecon(in);
        }

        @Override
        public PoaResponseForRecon[] newArray(int size) {
            return new PoaResponseForRecon[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeLong(shipmentId);
        parcel.writeLong(taskId);
        parcel.writeString(referenceId);
        parcel.writeString(lbn);
        parcel.writeString(packageId);
        parcel.writeParcelable(poaResponse, i);
        parcel.writeByte((byte) (scanned?1:0));
        parcel.writeByte((byte) (mandatory?1:0));
        parcel.writeString(cashReceiptBarcode);
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

    public long getShipmentId() {
        return shipmentId;
    }

    public void setShipmentId(long shipmentId) {
        this.shipmentId = shipmentId;
    }

    public long getTaskId() {
        return taskId;
    }

    public void setTaskId(long taskId) {
        this.taskId = taskId;
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

    public String getPackageId() {
        return packageId;
    }

    public void setPackageId(String packageId) {
        this.packageId = packageId;
    }

    public PoaResponse getPoaResponse() {
        return poaResponse;
    }

    public void setPoaResponse(PoaResponse poaResponse) {
        this.poaResponse = poaResponse;
    }

    public boolean isScanned() {
        return scanned;
    }

    public void setScanned(boolean scanned) {
        this.scanned = scanned;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    public String getCashReceiptBarcode() {
        return cashReceiptBarcode;
    }

    public void setCashReceiptBarcode(String cashReceiptBarcode) {
        this.cashReceiptBarcode = cashReceiptBarcode;
    }
}
