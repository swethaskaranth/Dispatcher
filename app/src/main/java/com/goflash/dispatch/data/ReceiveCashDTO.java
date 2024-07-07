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
public class ReceiveCashDTO implements Parcelable, Rush {

    private Long total;
    private Long pendingBalance;
    private Long currentTripCashCollected;

    @RushList(classType = CashChequeCollectedDetailsDTO.class)
    private List<CashChequeCollectedDetailsDTO> currentTripBreakUp = new ArrayList<>();

    public ReceiveCashDTO(){}

    public ReceiveCashDTO(Long total, Long pendingBalance, Long currentTripCashCollected, List<CashChequeCollectedDetailsDTO> currentTripBreakUp) {
        this.total = total;
        this.pendingBalance = pendingBalance;
        this.currentTripCashCollected = currentTripCashCollected;
        this.currentTripBreakUp = currentTripBreakUp;
    }

    protected ReceiveCashDTO(Parcel in) {
        if (in.readByte() == 0) {
            total = null;
        } else {
            total = in.readLong();
        }
        if (in.readByte() == 0) {
            pendingBalance = null;
        } else {
            pendingBalance = in.readLong();
        }
        if (in.readByte() == 0) {
            currentTripCashCollected = null;
        } else {
            currentTripCashCollected = in.readLong();
        }
        currentTripBreakUp = in.createTypedArrayList(CashChequeCollectedDetailsDTO.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (total == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(total);
        }
        if (pendingBalance == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(pendingBalance);
        }
        if (currentTripCashCollected == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(currentTripCashCollected);
        }
        dest.writeTypedList(currentTripBreakUp);
    }

    public static final Creator<ReceiveCashDTO> CREATOR = new Creator<ReceiveCashDTO>() {
        @Override
        public ReceiveCashDTO createFromParcel(Parcel in) {
            return new ReceiveCashDTO(in);
        }

        @Override
        public ReceiveCashDTO[] newArray(int size) {
            return new ReceiveCashDTO[size];
        }
    };

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public Long getPendingBalance() {
        return pendingBalance;
    }

    public void setPendingBalance(Long pendingBalance) {
        this.pendingBalance = pendingBalance;
    }

    public Long getCurrentTripCashCollected() {
        return currentTripCashCollected;
    }

    public void setCurrentTripCashCollected(Long currentTripCashCollected) {
        this.currentTripCashCollected = currentTripCashCollected;
    }

    public List<CashChequeCollectedDetailsDTO> getCurrentTripBreakUp() {
        return currentTripBreakUp;
    }

    public void setCurrentTripBreakUp(List<CashChequeCollectedDetailsDTO> currentTripBreakUp) {
        this.currentTripBreakUp = currentTripBreakUp;
    }

    public static Creator<ReceiveCashDTO> getCREATOR() {
        return CREATOR;
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
        return "ReceiveCashDTO{" +
                "total=" + total +
                ", pendingBalance=" + pendingBalance +
                ", currentTripCashCollected=" + currentTripCashCollected +
                ", currentTripBreakUp=" + currentTripBreakUp +
                '}';
    }
}
