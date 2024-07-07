package com.goflash.dispatch.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.List;

import co.uk.rushorm.core.Rush;
import co.uk.rushorm.core.RushCallback;
import co.uk.rushorm.core.RushCore;
import co.uk.rushorm.core.annotations.RushList;

public class ReceiveCdsCash implements Rush, Parcelable {

    private Double total;

    @RushList(classType = CdsCashCollection.class)
    private List<CdsCashCollection> currentTripBreakUp;

    public ReceiveCdsCash() {

    }

    public ReceiveCdsCash(Double total, List<CdsCashCollection> currentTripBreakUp) {
        this.total = total;
        this.currentTripBreakUp = currentTripBreakUp;
    }

    protected ReceiveCdsCash(Parcel in) {
        if (in.readByte() == 0) {
            total = null;
        } else {
            total = in.readDouble();
        }
        currentTripBreakUp = in.createTypedArrayList(CdsCashCollection.CREATOR);
    }

    public static final Creator<ReceiveCdsCash> CREATOR = new Creator<ReceiveCdsCash>() {
        @Override
        public ReceiveCdsCash createFromParcel(Parcel in) {
            return new ReceiveCdsCash(in);
        }

        @Override
        public ReceiveCdsCash[] newArray(int size) {
            return new ReceiveCdsCash[size];
        }
    };

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public List<CdsCashCollection> getCurrentTripBreakUp() {
        return currentTripBreakUp;
    }

    public void setCurrentTripBreakUp(List<CdsCashCollection> currentTripBreakUp) {
        this.currentTripBreakUp = currentTripBreakUp;
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
        if (total == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeDouble(total);
        }
        parcel.writeTypedList(currentTripBreakUp);
    }
}