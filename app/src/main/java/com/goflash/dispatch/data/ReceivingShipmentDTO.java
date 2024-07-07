package com.goflash.dispatch.data;

import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.List;

import co.uk.rushorm.core.Rush;
import co.uk.rushorm.core.RushCallback;
import co.uk.rushorm.core.RushCore;
import co.uk.rushorm.core.annotations.RushList;

public class ReceivingShipmentDTO implements Rush, Serializable {
    int shipmentId;
    String shipmentType;
    String status;
    String wayBillNumber;
    int partnerId;
    String partnerName;
    String referenceId;
   String returnState;
    String deliveryType;
    String lbn;
    boolean courier;
   boolean multiPartShipment;
    int multiPartShipmentCount;
    int multiPartShipmentReceivedCount;

    @RushList(classType = ReceivingChildShipmentDTO.class)
    List<ReceivingChildShipmentDTO> childShipments;

    ReceiveShipmentConfig configuration;


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

    public int getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(int partnerId) {
        this.partnerId = partnerId;
    }

    public String getPartnerName() {
        return partnerName;
    }

    public void setPartnerName(String partnerName) {
        this.partnerName = partnerName;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
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

    public boolean isMultiPartShipment() {
        return multiPartShipment;
    }

    public void setMultiPartShipment(boolean multiPartShipment) {
        this.multiPartShipment = multiPartShipment;
    }

    public int getMultiPartShipmentCount() {
        return multiPartShipmentCount;
    }

    public void setMultiPartShipmentCount(int multiPartShipmentCount) {
        this.multiPartShipmentCount = multiPartShipmentCount;
    }

    public int getMultipartReceivedShipmentCount() {
        return multiPartShipmentReceivedCount;
    }

    public void setMultipartReceivedShipmentCount(int multipartReceivedShipmentCount) {
        this.multiPartShipmentReceivedCount = multipartReceivedShipmentCount;
    }

    public List<ReceivingChildShipmentDTO> getChildShipments() {
        return childShipments;
    }

    public void setChildShipments(List<ReceivingChildShipmentDTO> childShipments) {
        this.childShipments = childShipments;
    }

    public ReceiveShipmentConfig getConfiguration() {
        return configuration;
    }

    public void setConfiguration(ReceiveShipmentConfig configuration) {
        this.configuration = configuration;
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
