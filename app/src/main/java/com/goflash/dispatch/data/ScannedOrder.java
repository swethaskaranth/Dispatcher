package com.goflash.dispatch.data;

import android.os.Parcel;
import android.os.Parcelable;

import co.uk.rushorm.core.Rush;
import co.uk.rushorm.core.RushCallback;
import co.uk.rushorm.core.RushCore;

public final class ScannedOrder implements Parcelable, Rush {

    private String referenceId;
    private String packageId;
    private String lbn;
    private String shipmentId;
    private String colourCode;
    private String status;
    private String binNumber;
    private boolean dispatchable;
    private boolean scanned = false;
    private String shipmentType;
    private String binBagDestination;
    private String scannedBarcode;
    private boolean receivedAtFinalDestination;

    public ScannedOrder() {

    }

    public ScannedOrder(String referenceId,String packageId, String lbn, String shipmentId, String colourCode, String status, String binNumber, boolean dispatchable, String shipmentType, String bagDestination, boolean receivedAtDestination) {
        this.referenceId = referenceId;
        this.packageId = packageId;
        this.lbn = lbn;
        this.shipmentId = shipmentId;
        this.colourCode = colourCode;
        this.status = status;
        this.binNumber = binNumber;
        this.dispatchable = dispatchable;
        this.scanned = false;
        this.shipmentType = shipmentType;
        this.binBagDestination = bagDestination;
        this.receivedAtFinalDestination = receivedAtDestination;
    }

    protected ScannedOrder(Parcel in) {
        referenceId = in.readString();
        packageId = in.readString();
        lbn = in.readString();
        shipmentId = in.readString();
        colourCode = in.readString();
        status = in.readString();
        binNumber = in.readString();
        dispatchable = in.readByte() != 0;
        scanned = in.readByte() != 0;
        shipmentType = in.readString();
        binBagDestination = in.readString();
        receivedAtFinalDestination = in.readByte() != 0;
    }

    public static final Creator<ScannedOrder> CREATOR = new Creator<ScannedOrder>() {
        @Override
        public ScannedOrder createFromParcel(Parcel in) {
            return new ScannedOrder(in);
        }

        @Override
        public ScannedOrder[] newArray(int size) {
            return new ScannedOrder[size];
        }
    };

    public boolean isScanned() {
        return scanned;
    }

    public void setScanned(boolean scanned) {
        this.scanned = scanned;
    }

    public String getBinNumber() {
        return binNumber;
    }

    public void setBinNumber(String binNumber) {
        this.binNumber = binNumber;
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

    public String getColourCode() {
        return colourCode;
    }

    public void setColourCode(String colourCode) {
        this.colourCode = colourCode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isDispatchable() {
        return dispatchable;
    }

    public void setDispatchable(boolean dispatchable) {
        this.dispatchable = dispatchable;
    }

    public String getShipmentType() {
        return shipmentType;
    }

    public void setShipmentType(String shipmentType) {
        this.shipmentType = shipmentType;
    }

    public String getBinBagDestination() {
        return binBagDestination;
    }

    public void setBinBagDestination(String binBagDestination) {
        this.binBagDestination = binBagDestination;
    }

    public String getScannedBarcode() {
        return scannedBarcode;
    }

    public void setScannedBarcode(String scannedBarcode) {
        this.scannedBarcode = scannedBarcode;
    }

    public boolean isReceivedAtFinalDestination() {
        return receivedAtFinalDestination;
    }

    public void setReceivedAtFinalDestination(boolean receivedAtFinalDestination) {
        this.receivedAtFinalDestination = receivedAtFinalDestination;
    }

    @Override
    public String toString() {
        return "ScannedOrder{" +
                "referenceId='" + referenceId + '\'' +
                ", packageId='" + packageId + '\'' +
                ", lbn='" + lbn + '\'' +
                ", shipmentId='" + shipmentId + '\'' +
                ", colourCode='" + colourCode + '\'' +
                ", status='" + status + '\'' +
                ", binNumber='" + binNumber + '\'' +
                ", dispatchable=" + dispatchable +
                ", scanned=" + scanned +
                ", shipmentType='" + shipmentType + '\'' +
                ", binBagDestination='" + binBagDestination + '\'' +
                '}';
    }

    public void save() {
        RushCore.getInstance().saveOnlyWithoutConflict(this);
    }

    public void save(RushCallback callback) {
        RushCore.getInstance().save(this, callback);
    }

    public void delete() {
        RushCore.getInstance().delete((Rush) this);
    }

    public void delete(RushCallback callback) {
        RushCore.getInstance().delete(this, callback);
    }

    @Override
    public String getId() {
        return RushCore.getInstance().getId(this);
    }

    @Override
    public boolean equals(Object obj) {
        return ((((ScannedOrder) obj).packageId!= null && ((ScannedOrder) obj).packageId.equals(this.packageId) )
                || (((ScannedOrder) obj).lbn!= null && ((ScannedOrder) obj).lbn.equals(this.lbn) )
                || (((ScannedOrder) obj).referenceId!= null && ((ScannedOrder) obj).referenceId.equals(this.referenceId) ));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(referenceId);
        parcel.writeString(packageId);
        parcel.writeString(lbn);
        parcel.writeString(shipmentId);
        parcel.writeString(colourCode);
        parcel.writeString(status);
        parcel.writeString(binNumber);
        parcel.writeByte((byte) (dispatchable ? 1 : 0));
        parcel.writeByte((byte) (scanned ? 1 : 0));
        parcel.writeString(shipmentType);
        parcel.writeString(binBagDestination);
        parcel.writeByte((byte) (receivedAtFinalDestination ? 1 : 0));
    }
}
