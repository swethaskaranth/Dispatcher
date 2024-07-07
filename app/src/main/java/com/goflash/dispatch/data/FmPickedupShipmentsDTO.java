package com.goflash.dispatch.data;

import android.os.Parcel;
import android.os.Parcelable;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

import co.uk.rushorm.core.Rush;
import co.uk.rushorm.core.RushCallback;
import co.uk.rushorm.core.RushCore;

public class FmPickedupShipmentsDTO implements Parcelable, Rush {

    Map<String, List<FmPickedShipment>> shipments;

    public FmPickedupShipmentsDTO() {
    }

    public FmPickedupShipmentsDTO(Map<String, List<FmPickedShipment>> shipments) {
        this.shipments = shipments;
    }

    protected FmPickedupShipmentsDTO(Parcel in){

    }

    public static final Creator<FmPickedupShipmentsDTO> CREATOR = new Creator<FmPickedupShipmentsDTO>() {
        @Override
        public FmPickedupShipmentsDTO createFromParcel(Parcel in) {
            return new FmPickedupShipmentsDTO(in);
        }

        @Override
        public FmPickedupShipmentsDTO[] newArray(int size) {
            return new FmPickedupShipmentsDTO[size];
        }
    };

    public Map<String, List<FmPickedShipment>> getShipments() {
        return shipments;
    }

    public void setShipments(Map<String, List<FmPickedShipment>> shipments) {
        this.shipments = shipments;
    }

    public void save() {
        RushCore.getInstance().saveOnlyWithoutConflict(this);
    }

    public void save(@NotNull RushCallback callback) {
        RushCore.getInstance().save(this, callback);
    }

    public void delete() {
        RushCore.getInstance().delete(this);
    }

    public void delete(@NotNull RushCallback callback) {
        RushCore.getInstance().delete(this, callback);
    }

    @NotNull
    public String getId() {
        return RushCore.getInstance().getId(this);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {


    }
}
