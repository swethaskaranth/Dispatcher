package com.goflash.dispatch.data;

import android.os.Parcel;
import android.os.Parcelable;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import co.uk.rushorm.core.Rush;
import co.uk.rushorm.core.RushCallback;
import co.uk.rushorm.core.RushCore;
import co.uk.rushorm.core.annotations.RushList;

public class ReturnShipmentDTO implements Parcelable, Rush {

    private String shipmentId;
    private String lbn;
    private String referenceId;
    private String packageId;
    private String type;
    private int expectedQuantity;
    private boolean partialDelivery;
    private boolean scanned;
    private String status;

    @RushList(classType = Item.class)
    private List<Item> items = new ArrayList<>();

    private ReturnReconImageDTO reconImages;

    public ReturnShipmentDTO() {

    }

    public ReturnShipmentDTO(String shipmentId, String lbn, String referenceId, String packageId, String type, int expectedQuantity, boolean partialDelivery, boolean scanned, String status, ReturnReconImageDTO reconImages) {
        this.shipmentId = shipmentId;
        this.lbn = lbn;
        this.referenceId = referenceId;
        this.packageId = packageId;
        this.type = type;
        this.expectedQuantity = expectedQuantity;
        this.partialDelivery = partialDelivery;
        this.scanned = scanned;
        if (status == null)
            this.status = "Pending";
        else
            this.status = status;
        this.reconImages = reconImages;
    }

    protected ReturnShipmentDTO(Parcel in) {
        shipmentId = in.readString();
        referenceId = in.readString();
        lbn = in.readString();
        packageId = in.readString();
        type = in.readString();
        expectedQuantity = in.readInt();
        partialDelivery = in.readByte() != 0;
        scanned = in.readByte() != 0;
        items = in.readParcelable(Item.class.getClassLoader());
        status = in.readString();
        reconImages = in.readParcelable(ReconImageDTO.class.getClassLoader());
    }

    public String getShipmentId() {
        return shipmentId;
    }

    public void setShipmentId(String shipmentId) {
        this.shipmentId = shipmentId;
    }

    public String getLbn() {
        return lbn;
    }

    public void setLbn(String lbn) {
        this.lbn = lbn;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public String getPackageId() {
        return packageId;
    }

    public void setPackageId(String packageId) {
        this.packageId = packageId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getExpectedQuantity() {
        return expectedQuantity;
    }

    public void setExpectedQuantity(int expectedQuantity) {
        this.expectedQuantity = expectedQuantity;
    }

    public boolean isPartialDelivery() {
        return partialDelivery;
    }

    public void setPartialDelivery(boolean partialDelivery) {
        this.partialDelivery = partialDelivery;
    }

    public boolean isScanned() {
        return scanned;
    }

    public void setScanned(boolean scanned) {
        this.scanned = scanned;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public String getStatus() {
        if (status == null)
            status = "Pending";
        return status;
    }

    public void setStatus(String status) {
        if (status == null)
            this.status = "Pending";
        else
            this.status = status;
    }

    public ReturnReconImageDTO getReconImages() {
        return reconImages;
    }

    public void setReconImages(ReturnReconImageDTO reconImages) {
        this.reconImages = reconImages;
    }

    public static final Creator<ReturnShipmentDTO> CREATOR = new Creator<ReturnShipmentDTO>() {
        @Override
        public ReturnShipmentDTO createFromParcel(Parcel in) {
            return new ReturnShipmentDTO(in);
        }

        @Override
        public ReturnShipmentDTO[] newArray(int size) {
            return new ReturnShipmentDTO[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(shipmentId);
        dest.writeString(referenceId);
        dest.writeString(lbn);
        dest.writeString(packageId);
        dest.writeString(type);
        dest.writeInt(expectedQuantity);
        dest.writeByte((byte) (partialDelivery ? 1 : 0));
        dest.writeByte((byte) (scanned ? 1 : 0));
        dest.writeTypedList(items);
        dest.writeString(status);
        dest.writeParcelable(reconImages, flags);

    }

    public static Creator<ReturnShipmentDTO> getCREATOR() {
        return CREATOR;
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
    public String toString() {
        return "ReturnShipmentDTO{" +
                "shipmentId='" + shipmentId + '\'' +
                ", lbn='" + lbn + '\'' +
                ", referenceId='" + referenceId + '\'' +
                ", packageId='" + packageId + '\'' +
                ", type='" + type + '\'' +
                ", expectedQuantity=" + expectedQuantity +
                ", partialDelivery=" + partialDelivery +
                '}';
    }
}
