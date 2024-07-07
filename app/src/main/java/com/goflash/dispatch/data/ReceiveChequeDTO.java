package com.goflash.dispatch.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

import co.uk.rushorm.core.Rush;
import co.uk.rushorm.core.RushCallback;
import co.uk.rushorm.core.RushCore;
import co.uk.rushorm.core.annotations.RushList;

/**
 * Created by Ravi on 2020-06-18.
 */
public class ReceiveChequeDTO implements Parcelable, Rush {

    private Integer count;
    private Double currentTripChequeCollected;

    @RushList(classType = CashChequeCollectedDetailsDTO.class)
    private List<CashChequeCollectedDetailsDTO> currentTripBreakUp = new ArrayList<>();

    public ReceiveChequeDTO(){}

    public ReceiveChequeDTO(Integer count, Double currentTripChequeCollected, List<CashChequeCollectedDetailsDTO> currentTripBreakUp) {
        this.count = count;
        this.currentTripChequeCollected = currentTripChequeCollected;
        this.currentTripBreakUp = currentTripBreakUp;
    }

    protected ReceiveChequeDTO(Parcel in) {
        if (in.readByte() == 0) {
            count = null;
        } else {
            count = in.readInt();
        }
        if (in.readByte() == 0) {
            currentTripChequeCollected = null;
        } else {
            currentTripChequeCollected = in.readDouble();
        }
        currentTripBreakUp = in.createTypedArrayList(CashChequeCollectedDetailsDTO.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (count == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(count);
        }
        if (currentTripChequeCollected == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(currentTripChequeCollected);
        }
        dest.writeTypedList(currentTripBreakUp);
    }

    public static final Creator<ReceiveChequeDTO> CREATOR = new Creator<ReceiveChequeDTO>() {
        @Override
        public ReceiveChequeDTO createFromParcel(Parcel in) {
            return new ReceiveChequeDTO(in);
        }

        @Override
        public ReceiveChequeDTO[] newArray(int size) {
            return new ReceiveChequeDTO[size];
        }
    };

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Double getCurrentTripChequeCollected() {
        return currentTripChequeCollected;
    }

    public void setCurrentTripChequeCollected(Double currentTripChequeCollected) {
        this.currentTripChequeCollected = currentTripChequeCollected;
    }

    public List<CashChequeCollectedDetailsDTO> getCurrentTripBreakUp() {
        return currentTripBreakUp;
    }

    public void setCurrentTripBreakUp(List<CashChequeCollectedDetailsDTO> currentTripBreakUp) {
        this.currentTripBreakUp = currentTripBreakUp;
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
    public String toString() {
        return "ReceiveChequeDTO{" +
                "count=" + count +
                ", currentTripChequeCollected=" + currentTripChequeCollected +
                ", currentTripBreakUp=" + currentTripBreakUp +
                '}';
    }
}
