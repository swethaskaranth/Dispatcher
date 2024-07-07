package com.goflash.dispatch.data;

import android.os.Parcel;
import android.os.Parcelable;

import co.uk.rushorm.core.Rush;
import co.uk.rushorm.core.RushCallback;
import co.uk.rushorm.core.RushCore;

/**
 * Created by Ravi on 2020-06-18.
 */
public class TripDTO implements Parcelable, Rush {

    private Long id;
    private Long tripId;
    private String agentId;
    private String status;
    private String agentName="";
    private Long taskCount;
    private double cashToBeCollected;
    private double cashCollected;
    private ReceiveCdsCash cdsCashCollection;
    private String routeId;
    private String bin="";
    private String phoneNumber;
    private String updatedOn;

    public TripDTO(){

    }

    public TripDTO(Long id,Long tripId, String agentId, String status, String agentName, Long taskCount, double cashToBeCollected, double cashCollected, String routeId, String bin, String phoneNumber, String updatedOn) {
        this.id = id;
        this.tripId = tripId;
        this.agentId = agentId;
        this.status = status;
        this.agentName = agentName;
        this.taskCount = taskCount;
        this.cashToBeCollected = cashToBeCollected;
        this.cashCollected = cashCollected;
        this.routeId = routeId;
        this.bin = bin;
        this.phoneNumber = phoneNumber;
        this.updatedOn = updatedOn;
    }

    protected TripDTO(Parcel in) {
        id = in.readLong();
        tripId = in.readLong();
        agentId = in.readString();
        status = in.readString();
        agentName = in.readString();
        if (in.readByte() == 0) {
            taskCount = null;
        } else {
            taskCount = in.readLong();
        }
        if (in.readByte() == 0.0) {
            cashToBeCollected = 0.0;
        } else {
            cashToBeCollected = in.readDouble();
        }
        if (in.readByte() == 0) {
            cashCollected = 0.0;
        } else {
            cashCollected = in.readLong();
        }
        routeId = in.readString();
        bin = in.readString();
        phoneNumber = in.readString();
        cdsCashCollection = in.readParcelable(ReceiveCdsCash.CREATOR.getClass().getClassLoader());
    }

    public static final Creator<TripDTO> CREATOR = new Creator<TripDTO>() {
        @Override
        public TripDTO createFromParcel(Parcel in) {
            return new TripDTO(in);
        }

        @Override
        public TripDTO[] newArray(int size) {
            return new TripDTO[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(id);
        parcel.writeLong(tripId);
        parcel.writeString(agentId);
        parcel.writeString(status);
        parcel.writeString(agentName);
        parcel.writeLong(taskCount);
        parcel.writeDouble(cashToBeCollected);
        parcel.writeDouble(cashCollected);
        parcel.writeString(routeId);
        parcel.writeString(bin);
        parcel.writeString(phoneNumber);
        parcel.writeParcelable(cdsCashCollection,i);
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIdForTrip(){
        return id;
    }

    public Long getTripId() {
        return tripId;
    }

    public void setTripId(Long tripId) {
        this.tripId = tripId;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    public Long getTaskCount() {
        return taskCount;
    }

    public void setTaskCount(Long taskCount) {
        this.taskCount = taskCount;
    }

    public double getCashCollected() {
        return cashCollected;
    }

    public void setCashCollected(Long cashCollected) {
        this.cashCollected = cashCollected;
    }

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public String getBin() {
        return bin;
    }

    public void setBin(String bin) {
        this.bin = bin;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setCashToBeCollected(double cashToBeCollected) {
        this.cashToBeCollected = cashToBeCollected;
    }

    public void setCashCollected(double cashCollected) {
        this.cashCollected = cashCollected;
    }

    public String getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(String updatedOn) {
        this.updatedOn = updatedOn;
    }

    public double getCashToBeCollected() {
        return cashToBeCollected;
    }

    public ReceiveCdsCash getCdsCashCollection() {
        return cdsCashCollection;
    }

    public void setCdsCashCollection(ReceiveCdsCash cdsCashCollection) {
        this.cdsCashCollection = cdsCashCollection;
    }

    public static Creator<TripDTO> getCREATOR() {
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
    public String toString() {
        return "TripDTO{" +
                "tripId='" + tripId + '\'' +
                ", agentId='" + agentId + '\'' +
                ", status='" + status + '\'' +
                ", agentName='" + agentName + '\'' +
                ", taskCount=" + taskCount +
                ", cashToBeCollected=" + cashToBeCollected +
                ", cashCollected=" + cashCollected +
                ", routeId='" + routeId + '\'' +
                ", bin='" + bin + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                '}';
    }
}
