package com.goflash.dispatch.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

import co.uk.rushorm.core.Rush;
import co.uk.rushorm.core.RushCallback;
import co.uk.rushorm.core.RushCore;

public class CdsCashCollection implements Rush, Parcelable {

    private String transactionId;
    private String partnerTransactionId;
    private Double amount;
    private long tripId;
    private String transactionDate;

    public CdsCashCollection() {

    }

    public CdsCashCollection(String transactionId, String partnerTransactionId, Double amount, long tripId, String transactionDate) {
        this.transactionId = transactionId;
        this.partnerTransactionId = partnerTransactionId;
        this.amount = amount;
        this.tripId = tripId;
        this.transactionDate = transactionDate;
    }

    protected CdsCashCollection(Parcel in) {
        transactionId = in.readString();
        partnerTransactionId = in.readString();
        if (in.readByte() == 0) {
            amount = null;
        } else {
            amount = in.readDouble();
        }
        tripId = in.readLong();
        transactionDate = in.readString();
    }

    public static final Creator<CdsCashCollection> CREATOR = new Creator<CdsCashCollection>() {
        @Override
        public CdsCashCollection createFromParcel(Parcel in) {
            return new CdsCashCollection(in);
        }

        @Override
        public CdsCashCollection[] newArray(int size) {
            return new CdsCashCollection[size];
        }
    };

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getPartnerTransactionId() {
        return partnerTransactionId;
    }

    public void setPartnerTransactionId(String partnerTransactionId) {
        this.partnerTransactionId = partnerTransactionId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public long getTripId() {
        return tripId;
    }

    public void setTripId(long tripId) {
        this.tripId = tripId;
    }

    public String getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(String transactionDate) {
        this.transactionDate = transactionDate;
    }

    public void save() {
        RushCore.getInstance().saveOnlyWithoutConflict(this);
    }

    public void save(RushCallback callback) {
        RushCore.getInstance().save(this, callback);
    }

    public void delete() {
        RushCore.getInstance().delete((Rush) this);
    }

    public void delete(RushCallback callback) {
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
        parcel.writeString(transactionId);
        parcel.writeString(partnerTransactionId);
        if (amount == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeDouble(amount);
        }
        parcel.writeLong(tripId);
        parcel.writeString(transactionDate);
    }
}