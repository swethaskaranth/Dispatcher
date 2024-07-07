package com.goflash.dispatch.data;

import android.os.Parcel;
import android.os.Parcelable;

import co.uk.rushorm.core.Rush;
import co.uk.rushorm.core.RushCallback;
import co.uk.rushorm.core.RushCore;

public class AckSlipDto implements Parcelable, Rush {

    private Long id;
    private String url;
    private String key;
    private String lbn;
    private String shipmentId;
    private String status;
    private String source;
    private Long tripId;

    public AckSlipDto(){}

    public AckSlipDto(Long id, String url, String key, String lbn, String shipmentId, String status, String source, Long tripId) {
        this.id = id;
        this.url = url;
        this.key = key;
        this.lbn = lbn;
        this.shipmentId = shipmentId;
        this.status = status;
        this.source = source;
        this.tripId = tripId;
    }

    public AckSlipDto(Long id, String url, String lbn){
        this.id = id;
        this.url = url;
        this.lbn = lbn;
    }

    public AckSlipDto(String key, String url, String lbn){
        this.key = key;
        this.url = url;
        this.lbn = lbn;
    }

    protected AckSlipDto(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readLong();
        }
        url = in.readString();
        key = in.readString();
        lbn = in.readString();
        shipmentId = in.readString();
        tripId = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (id == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(id);
        }
        dest.writeString(url);
        dest.writeString(key);
        dest.writeString(lbn);
        dest.writeString(shipmentId);
        dest.writeLong(tripId);
    }

    public static final Creator<AckSlipDto> CREATOR = new Creator<AckSlipDto>() {
        @Override
        public AckSlipDto createFromParcel(Parcel in) {
            return new AckSlipDto(in);
        }

        @Override
        public AckSlipDto[] newArray(int size) {
            return new AckSlipDto[size];
        }
    };

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAckSlipId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getLbn() {
        return lbn;
    }

    public void setLbn(String lbn) {
        this.lbn = lbn;
    }

    public String getShipmentId() {
        return shipmentId;
    }

    public void setShipmentId(String shipmentId) {
        this.shipmentId = shipmentId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String type) {
        this.source = type;
    }

    public Long getTripId() {
        return tripId;
    }

    public void setTripId(Long tripId) {
        this.tripId = tripId;
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
