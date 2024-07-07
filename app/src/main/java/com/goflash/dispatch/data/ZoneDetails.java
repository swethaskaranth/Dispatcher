package com.goflash.dispatch.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class ZoneDetails implements  Parcelable {

    private String id;
    private String zoneName;
    private Integer shipmentCount;
    private Integer priorityCount;

    private List<SprinterForZone> sprinters = new ArrayList<>();

    public ZoneDetails() {
    }

    public ZoneDetails(String zoneId, String zoneName, Integer shipmentCount, Integer priorityCount, List<SprinterForZone> sprinters) {
        this.id = zoneId;
        this.zoneName = zoneName;
        this.shipmentCount = shipmentCount;
        this.priorityCount = priorityCount;
        this.sprinters = sprinters;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getZoneName() {
        return zoneName;
    }

    public void setZoneName(String zoneName) {
        this.zoneName = zoneName;
    }

    public Integer getShipmentCount() {
        return shipmentCount;
    }

    public void setShipmentCount(Integer shipmentCount) {
        this.shipmentCount = shipmentCount;
    }

    public Integer getPriorityCount() {
        return priorityCount;
    }

    public void setPriorityCount(Integer priorityCount) {
        this.priorityCount = priorityCount;
    }

    public List<SprinterForZone> getSprinters() {
        return sprinters;
    }

    public void setSprinters(List<SprinterForZone> sprinters) {
        this.sprinters = sprinters;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    private ZoneDetails(Parcel in) {
        id = in.readString();
        zoneName = in.readString();
        shipmentCount = in.readInt();
        priorityCount = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(zoneName);
        dest.writeInt(shipmentCount);
        dest.writeInt(priorityCount);

    }

    public static final Creator<ZoneDetails> CREATOR = new Creator<ZoneDetails>() {
        @Override
        public ZoneDetails createFromParcel(Parcel in) {
            return new ZoneDetails(in);
        }

        @Override
        public ZoneDetails[] newArray(int size) {
            return new ZoneDetails[size];
        }
    };

    public static Creator<ZoneDetails> getCREATOR() {
        return CREATOR;
    }

}
