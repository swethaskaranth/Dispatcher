package com.goflash.dispatch.data;

import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

import co.uk.rushorm.core.Rush;
import co.uk.rushorm.core.RushCallback;
import co.uk.rushorm.core.RushCore;

public class ReceivingChildShipmentDTO implements Rush, Serializable {

    int shipmentId;
    String shipmentType;
    String status;
    String wayBillNumber;
    String referenceId;
    boolean received;
    int receivedInRunId;
    String receivedStatus;
    String returnState;
    String deliveryType;
    String lbn;
    boolean courier;
    int parentShipmentId;


    public int getShipmentId() {
        return shipmentId;
    }

    public void setShipmentId(int shipmentId) {
        this.shipmentId = shipmentId;
    }

    public String getShipmentType() {
        return shipmentType;
    }

    public void setShipmentType(String shipmentType) {
        this.shipmentType = shipmentType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getWayBillNumber() {
        return wayBillNumber;
    }

    public void setWayBillNumber(String wayBillNumber) {
        this.wayBillNumber = wayBillNumber;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public boolean isReceived() {
        return received;
    }

    public void setReceived(boolean received) {
        this.received = received;
    }

    public int getReceivedInRunId() {
        return receivedInRunId;
    }

    public void setReceivedInRunId(int receivedInRunId) {
        this.receivedInRunId = receivedInRunId;
    }

    public String getReceivedStatus() {
        return receivedStatus;
    }

    public void setReceivedStatus(String receivedStatus) {
        this.receivedStatus = receivedStatus;
    }

    public String getReturnState() {
        return returnState;
    }

    public void setReturnState(String returnState) {
        this.returnState = returnState;
    }

    public String getDeliveryType() {
        return deliveryType;
    }

    public void setDeliveryType(String deliveryType) {
        this.deliveryType = deliveryType;
    }

    public String getLbn() {
        return lbn;
    }

    public void setLbn(String lbn) {
        this.lbn = lbn;
    }

    public boolean isCourier() {
        return courier;
    }

    public void setCourier(boolean courier) {
        this.courier = courier;
    }

    public int getParentShipmentId() {
        return parentShipmentId;
    }

    public void setParentShipmentId(int parentShipmentId) {
        this.parentShipmentId = parentShipmentId;
    }

    public void save() {
        RushCore.getInstance().saveOnlyWithoutConflict((Rush) this);
    }

    public void save(@NotNull RushCallback callback) {
        RushCore.getInstance().save((Rush) this, callback);
    }

    public void delete() {
        RushCore.getInstance().delete((Rush) this);
    }

    public void delete(@NotNull RushCallback callback) {
        RushCore.getInstance().delete((Rush) this, callback);
    }

    @NotNull
    public String getId() {
        return RushCore.getInstance().getId(this);
    }
}
