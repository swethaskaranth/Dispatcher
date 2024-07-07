package com.goflash.dispatch.data;

import android.os.Parcel;
import android.os.Parcelable;
import co.uk.rushorm.core.Rush;
import co.uk.rushorm.core.RushCallback;
import co.uk.rushorm.core.RushCore;

import java.util.ArrayList;

public final class BagDTO implements Parcelable, Rush {

    private String bagId;
    private String currentNodeName;
    private long currentNodeId;
    private ArrayList<ScannedOrder> shipmentList;
    private String destinationName;
    private Long destinationId;
    private String bagStatus;

    private Double weight;


    protected BagDTO(Parcel in) {
        bagId = in.readString();
        currentNodeName = in.readString();
        currentNodeId = in.readLong();
        shipmentList = in.createTypedArrayList(ScannedOrder.CREATOR);
        destinationName = in.readString();
        if (in.readByte() == 0) {
            destinationId = null;
        } else {
            destinationId = in.readLong();
        }
        bagStatus = in.readString();
        weight = in.readDouble();
    }

    public static final Creator<BagDTO> CREATOR = new Creator<BagDTO>() {
        @Override
        public BagDTO createFromParcel(Parcel in) {
            return new BagDTO(in);
        }

        @Override
        public BagDTO[] newArray(int size) {
            return new BagDTO[size];
        }
    };

    public void setBagId(String bagId) {
        this.bagId = bagId;
    }

    public void setCurrentNodeName(String currentNodeName) {
        this.currentNodeName = currentNodeName;
    }

    public void setCurrentNodeId(long currentNodeId) {
        this.currentNodeId = currentNodeId;
    }

    public void setShipmentList(ArrayList<ScannedOrder> shipmentList) {
        this.shipmentList = shipmentList;
    }

    public void setDestinationId(Long destinationId) {
        this.destinationId = destinationId;
    }

    public final String getBagId() {
        return this.bagId;
    }


    public final String getCurrentNodeName() {
        return this.currentNodeName;
    }

    public final long getCurrentNodeId() {
        return this.currentNodeId;
    }


    public final ArrayList<ScannedOrder> getShipmentList() {
        return this.shipmentList;
    }


    public final String getDestinationName() {
        return this.destinationName;
    }

    public final void setDestinationName(String var1) {
        this.destinationName = var1;
    }


    public final Long getDestinationId() {
        return this.destinationId;
    }

    public String getBagStatus() {
        return bagStatus;
    }

    public void setBagStatus(String bagStatus) {
        this.bagStatus = bagStatus;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public BagDTO() {

    }

    public BagDTO(String bagId, String currentNode, long currentNodeId, ArrayList<ScannedOrder> shipmentList, String destinationName, Long destinationId, String bagStatus, Double boxSize) {
        super();
        this.bagId = bagId;
        this.currentNodeName = currentNode;
        this.currentNodeId = currentNodeId;
        this.shipmentList = shipmentList;
        this.destinationName = destinationName;
        this.destinationId = destinationId;
        this.bagStatus = bagStatus;
        this.weight = boxSize;
    }


    public String toString() {
        return "BagDto(bagId=" + this.bagId + ", currentNodeName=" + this.currentNodeName + ", currentNodeId=" + this.currentNodeId + ", shipmentList=" + this.shipmentList + ", destinationName=" + this.destinationName + ", destinationId=" + this.destinationId + ")";
    }


    public boolean equals(Object bag) {
        if (bag instanceof BagDTO)
            return this.bagId.equals(((BagDTO) bag).bagId);
        else
            return false;
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
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(bagId);
        parcel.writeString(currentNodeName);
        parcel.writeLong(currentNodeId);
        parcel.writeTypedList(shipmentList);
        parcel.writeString(destinationName);
        if (destinationId == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeLong(destinationId);
        }
        parcel.writeString(bagStatus);
        if (weight != null)
            parcel.writeDouble(weight);
        else
            parcel.writeDouble(0.0);
    }
}
