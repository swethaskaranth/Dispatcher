package com.goflash.dispatch.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

import co.uk.rushorm.core.Rush;
import co.uk.rushorm.core.RushCallback;
import co.uk.rushorm.core.RushCore;
import co.uk.rushorm.core.annotations.RushList;

public class ReconImageDTO implements Parcelable, Rush {

    private String pathString;
    private List<String> paths;

    private String shipmentId;

    public ReconImageDTO(){

    }

    public ReconImageDTO(List<String> paths, String shipmentId) {
        this.shipmentId = shipmentId;
        this.paths = paths;
        setPathString(String.join(",",this.paths));
    }

    public List<String> getPaths() {
        return paths;
    }

    public void setPaths(List<String> paths) {
        this.paths = paths;
        setPathString(String.join(",",this.paths));
    }

    public String getPathString() {
        return pathString;
    }

    public void setPathString(String pathString) {
        this.pathString = pathString;
    }

    public String getShipmentId() {
        return shipmentId;
    }

    public void setShipmentId(String shipmentId) {
        this.shipmentId = shipmentId;
    }

    protected ReconImageDTO(Parcel in) {
        shipmentId = in.readString();
        paths = in.createStringArrayList();
        pathString = in.readString();
    }

    public static final Creator<ReconImageDTO> CREATOR = new Creator<ReconImageDTO>() {
        @Override
        public ReconImageDTO createFromParcel(Parcel in) {
            return new ReconImageDTO(in);
        }

        @Override
        public ReconImageDTO[] newArray(int size) {
            return new ReconImageDTO[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(shipmentId);
        parcel.writeStringList(paths);
        parcel.writeString(pathString);
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
}
