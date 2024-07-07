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
public class ReceiveNetBankDTO implements Parcelable, Rush {

    private Integer count;
    private Double currentTripNetBankCollected;

    @RushList(classType = CashChequeCollectedDetailsDTO.class)
    private List<CashChequeCollectedDetailsDTO> currentTripBreakUp = new ArrayList<>();

    public ReceiveNetBankDTO(){}

    public ReceiveNetBankDTO(Integer count, Double currentTripNetBankCollected, List<CashChequeCollectedDetailsDTO> currentTripBreakUp) {
        this.count = count;
        this.currentTripNetBankCollected = currentTripNetBankCollected;
        this.currentTripBreakUp = currentTripBreakUp;
    }

    protected ReceiveNetBankDTO(Parcel in) {
        if (in.readByte() == 0) {
            count = null;
        } else {
            count = in.readInt();
        }
        if (in.readByte() == 0) {
            currentTripNetBankCollected = null;
        } else {
            currentTripNetBankCollected = in.readDouble();
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
        if (currentTripNetBankCollected == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(currentTripNetBankCollected);
        }
        dest.writeTypedList(currentTripBreakUp);
    }

    public static final Creator<ReceiveNetBankDTO> CREATOR = new Creator<ReceiveNetBankDTO>() {
        @Override
        public ReceiveNetBankDTO createFromParcel(Parcel in) {
            return new ReceiveNetBankDTO(in);
        }

        @Override
        public ReceiveNetBankDTO[] newArray(int size) {
            return new ReceiveNetBankDTO[size];
        }
    };

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Double getCurrentTripNetBankCollected() {
        return currentTripNetBankCollected;
    }

    public void setCurrentTripNetBankCollected(Double currentTripNetBankCollected) {
        this.currentTripNetBankCollected = currentTripNetBankCollected;
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
        return "ReceiveNetBankDTO{" +
                "count=" + count +
                ", currentTripChequeCollected=" + currentTripNetBankCollected +
                ", currentTripBreakUp=" + currentTripBreakUp +
                '}';
    }
}
