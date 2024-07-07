package com.goflash.dispatch.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

import co.uk.rushorm.core.Rush;
import co.uk.rushorm.core.RushCallback;
import co.uk.rushorm.core.RushCore;
import co.uk.rushorm.core.annotations.RushList;

public class AckForRecon implements Parcelable, Rush {

    private String displayId;

    @RushList(classType = AckSlipDto.class)
    private List<AckSlipDto> ackList;

    private Long tripId;

    private String lbn;

    public AckForRecon() {

    }

    public AckForRecon(String displayId, List<AckSlipDto> ackList, Long tripId) {
        this.displayId = displayId;
        this.ackList = ackList;
        this.tripId = tripId;
        this.lbn = lbn;
    }

    public AckForRecon(Parcel in){
        this.displayId = in.readString();
        this.ackList = in.createTypedArrayList(AckSlipDto.CREATOR);
        this.tripId = in.readLong();
        this.lbn = in.readString();
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(displayId);
        parcel.writeTypedList(ackList);
        parcel.writeLong(tripId);
        parcel.writeString(lbn);
    }

    public static final Creator<AckForRecon> CREATOR = new Creator<AckForRecon>() {
        @Override
        public AckForRecon createFromParcel(Parcel in) {
            return new AckForRecon(in);
        }

        @Override
        public AckForRecon[] newArray(int size) {
            return new AckForRecon[size];
        }
    };

    public String getDisplayId() {
        return displayId;
    }

    public void setDisplayId(String displayId) {
        this.displayId = displayId;
    }

    public List<AckSlipDto> getAckList() {
        return ackList;
    }

    public void setAckList(List<AckSlipDto> ackList) {
        this.ackList = ackList;
    }

    public Long getTripId() {
        return tripId;
    }

    public void setTripId(Long tripId) {
        this.tripId = tripId;
    }

    public String getLbn() {
        return lbn;
    }

    public void setLbn(String lbn) {
        this.lbn = lbn;
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
}
