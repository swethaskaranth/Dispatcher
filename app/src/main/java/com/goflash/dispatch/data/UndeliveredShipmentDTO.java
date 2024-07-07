package com.goflash.dispatch.data;

import android.os.Parcel;
import android.os.Parcelable;

import co.uk.rushorm.core.Rush;
import co.uk.rushorm.core.RushCallback;
import co.uk.rushorm.core.RushCore;

/**
 * Created by Ravi on 2020-06-18.
 */
public class UndeliveredShipmentDTO implements Parcelable, Rush {

    private String shipmentId;
    private String lbn;
    private String referenceId;
    private String packageId;
    private String type;
    private String status;
    private String shipmentStatus;
    private String postponedToDate;
    private String slotStart;
    private String slotEnd;
    private boolean scanned = false;
    private String reason;
    private Long updated;

    private MetaDetails metaDetails;

    protected UndeliveredShipmentDTO(Parcel in) {
        shipmentId = in.readString();
        lbn = in.readString();
        referenceId = in.readString();
        packageId = in.readString();
        type = in.readString();
        status = in.readString();
        shipmentStatus = in.readString();
        postponedToDate = in.readString();
        slotStart = in.readString();
        slotEnd = in.readString();
        scanned = in.readByte() != 0;
        reason = in.readString();
        updated = in.readLong();
        metaDetails = in.readParcelable(MetaDetails.class.getClassLoader());
    }

    public static final Creator<UndeliveredShipmentDTO> CREATOR = new Creator<UndeliveredShipmentDTO>() {
        @Override
        public UndeliveredShipmentDTO createFromParcel(Parcel in) {
            return new UndeliveredShipmentDTO(in);
        }

        @Override
        public UndeliveredShipmentDTO[] newArray(int size) {
            return new UndeliveredShipmentDTO[size];
        }
    };

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
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(shipmentId);
        dest.writeString(lbn);
        dest.writeString(referenceId);
        dest.writeString(packageId);
        dest.writeString(type);
        dest.writeString(status);
        dest.writeString(shipmentStatus);
        dest.writeString(postponedToDate);
        dest.writeString(slotStart);
        dest.writeString(slotEnd);
        dest.writeByte((byte) (scanned ? 1 : 0));
        dest.writeString(reason);
        dest.writeLong(updated);
        dest.writeParcelable(metaDetails, flags);
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getShipmentStatus() {
        return shipmentStatus;
    }

    public void setShipmentStatus(String shipmentStatus) {
        this.shipmentStatus = shipmentStatus;
    }

    public String getPostponedToDate() {
        return postponedToDate;
    }

    public void setPostponedToDate(String postponedToDate) {
        this.postponedToDate = postponedToDate;
    }

    public String getSlotStart() {
        return slotStart;
    }

    public void setSlotStart(String slotStart) {
        this.slotStart = slotStart;
    }

    public String getSlotEnd() {
        return slotEnd;
    }

    public void setSlotEnd(String slotEnd) {
        this.slotEnd = slotEnd;
    }

    public boolean isScanned() {
        return scanned;
    }

    public void setScanned(boolean scanned) {
        this.scanned = scanned;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Long getUpdated() {
        return updated;
    }

    public void setUpdated(Long updated) {
        this.updated = updated;
    }

    public MetaDetails getMetaDetails() {
        return metaDetails;
    }

    public void setMetaDetails(MetaDetails metaDetails) {
        this.metaDetails = metaDetails;
    }

    public static Creator<UndeliveredShipmentDTO> getCREATOR() {
        return CREATOR;
    }

    public UndeliveredShipmentDTO(){}

    public UndeliveredShipmentDTO(String shipmentId, String lbn, String referenceId, String packageId, String type, String status,String shipmentStatus, String postponedToDate, String slotStart, String slotEnd, boolean scanned, String reason, Long updated, MetaDetails metaDetails) {
        this.shipmentId = shipmentId;
        this.lbn = lbn;
        this.referenceId = referenceId;
        this.packageId = packageId;
        this.type = type;
        this.status = status;
        this.shipmentStatus = shipmentStatus;
        this.postponedToDate = postponedToDate;
        this.slotStart = slotStart;
        this.slotEnd = slotEnd;
        this.scanned = scanned;
        this.reason = reason;
        this.updated = updated;
        this.metaDetails = metaDetails;
    }

    @Override
    public String toString() {
        return "UndeliveredShipmentDTO{" +
                "shipmentId='" + shipmentId + '\'' +
                ", lbn='" + lbn + '\'' +
                ", referenceId='" + referenceId + '\'' +
                ", packageId='" + packageId + '\'' +
                ", type='" + type + '\'' +
                ", status='" + status + '\'' +
                ", postponedToDate='" + postponedToDate + '\'' +
                ", slotStart='" + slotStart + '\'' +
                ", slotEnd='" + slotEnd + '\'' +
                ", scanned=" + scanned +
                ", reason=" + reason +
                ", updated=" + updated +
                '}';
    }
}
