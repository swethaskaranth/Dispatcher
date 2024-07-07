package com.goflash.dispatch.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.goflash.dispatch.type.ShipmentType;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import co.uk.rushorm.core.Rush;
import co.uk.rushorm.core.RushCallback;
import co.uk.rushorm.core.RushCore;
import co.uk.rushorm.core.annotations.RushList;

/**
 * Created by Ravi on 2020-06-19.
 */
public class UnassignedDTO implements Parcelable, Rush {

    private Long id;
    private String shipmentId;
    private String referenceId;
    private String orderId;
    private String assetName;
    private String name;
    private String address1;
    private String address2;
    private String address3;
    private String city;
    private String state;
    private String pincode;
    private String paymentType;
    private Double shipmentValue;
    private String customerReferenceId;
    private String createdOn;
    private String updatedOn;

    @RushList(classType = Item.class)
    private List<Item> items = new ArrayList<>();

    private String type;
    private String status;
    private Long mposPaymentId;
    private String lbn;
    private String currentBin;
    private String committedExpectedDeliveryDate = "";
    private Boolean partialDelivery = true;
    private String postponedToDate;
    private String slotStart;
    private String slotEnd;
    private Boolean processingBlocked;
    private String serviceType;
    private String consumerType;
    private String clientCode;
    private String shipmentType;
    private String allowPaymentMode;
    private String priorityType;
    private Boolean allowCheckPayment;
    private String packageId;
    private String routeId;
    private String adhocCreateType;
    private String zoneName;
    private Integer zoneId;
    private String pickupPincode;
    private String dropPincode;
    private String parentShipment;

    private Calendar customerDefinedSlotStartTime;
    private Calendar customerDefinedSlotEndTime;

    private Integer fmExpectedCount;

    private Boolean selected;

    private Integer mpsCount;

    private ShipmentMetaDetails shipmentMetaDetails;


    protected UnassignedDTO(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readLong();
        }
        shipmentId = in.readString();
        referenceId = in.readString();
        orderId = in.readString();
        assetName = in.readString();
        name = in.readString();
        address1 = in.readString();
        address2 = in.readString();
        address3 = in.readString();
        city = in.readString();
        state = in.readString();
        pincode = in.readString();
        paymentType = in.readString();
        if (in.readByte() == 0) {
            shipmentValue = null;
        } else {
            shipmentValue = in.readDouble();
        }
        customerReferenceId = in.readString();
        createdOn = in.readString();
        updatedOn = in.readString();
        items = in.createTypedArrayList(Item.CREATOR);
        type = in.readString();
        status = in.readString();
        if (in.readByte() == 0) {
            mposPaymentId = null;
        } else {
            mposPaymentId = in.readLong();
        }
        lbn = in.readString();
        currentBin = in.readString();
        committedExpectedDeliveryDate = in.readString();
        byte tmpPartialDelivery = in.readByte();
        partialDelivery = tmpPartialDelivery == 0 ? null : tmpPartialDelivery == 1;
        postponedToDate = in.readString();
        slotStart = in.readString();
        slotEnd = in.readString();
        byte tmpProcessingBlocked = in.readByte();
        processingBlocked = tmpProcessingBlocked == 0 ? null : tmpProcessingBlocked == 1;
        serviceType = in.readString();
        consumerType = in.readString();
        clientCode = in.readString();
        shipmentType = in.readString();
        allowPaymentMode = in.readString();
        priorityType = in.readString();
        byte tmpAllowCheckPayment = in.readByte();
        allowCheckPayment = tmpAllowCheckPayment == 0 ? null : tmpAllowCheckPayment == 1;
        packageId = in.readString();
        routeId = in.readString();
        adhocCreateType = in.readString();
        zoneName = in.readString();
        if (in.readByte() == 0) {
            zoneId = null;
        } else {
            zoneId = in.readInt();
        }
        pickupPincode = in.readString();
        dropPincode = in.readString();
        byte tmpSelected = in.readByte();
        selected = tmpSelected == 0 ? null : tmpSelected == 1;
        mpsCount = in.readInt();
        parentShipment = in.readString();
        fmExpectedCount = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (id == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(id);
        }
        dest.writeString(shipmentId);
        dest.writeString(referenceId);
        dest.writeString(orderId);
        dest.writeString(assetName);
        dest.writeString(name);
        dest.writeString(address1);
        dest.writeString(address2);
        dest.writeString(address3);
        dest.writeString(city);
        dest.writeString(state);
        dest.writeString(pincode);
        dest.writeString(paymentType);
        if (shipmentValue == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(shipmentValue);
        }
        dest.writeString(customerReferenceId);
        dest.writeString(createdOn);
        dest.writeString(updatedOn);
        dest.writeTypedList(items);
        dest.writeString(type);
        dest.writeString(status);
        if (mposPaymentId == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(mposPaymentId);
        }
        dest.writeString(lbn);
        dest.writeString(currentBin);
        dest.writeString(committedExpectedDeliveryDate);
        dest.writeByte((byte) (partialDelivery == null ? 0 : partialDelivery ? 1 : 2));
        dest.writeString(postponedToDate);
        dest.writeString(slotStart);
        dest.writeString(slotEnd);
        dest.writeByte((byte) (processingBlocked == null ? 0 : processingBlocked ? 1 : 2));
        dest.writeString(serviceType);
        dest.writeString(consumerType);
        dest.writeString(clientCode);
        dest.writeString(shipmentType);
        dest.writeString(allowPaymentMode);
        dest.writeString(priorityType);
        dest.writeByte((byte) (allowCheckPayment == null ? 0 : allowCheckPayment ? 1 : 2));
        dest.writeString(packageId);
        dest.writeString(routeId);
        dest.writeString(adhocCreateType);
        dest.writeString(zoneName);
        if (zoneId == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(zoneId);
        }
        dest.writeString(pickupPincode);
        dest.writeString(dropPincode);
        dest.writeByte((byte) (selected == null ? 0 : selected ? 1 : 2));

        dest.writeInt(mpsCount);
        dest.writeString(parentShipment);

        dest.writeInt(fmExpectedCount);

    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<UnassignedDTO> CREATOR = new Creator<UnassignedDTO>() {
        @Override
        public UnassignedDTO createFromParcel(Parcel in) {
            return new UnassignedDTO(in);
        }

        @Override
        public UnassignedDTO[] newArray(int size) {
            return new UnassignedDTO[size];
        }
    };

    public void setId(Long id) {
        this.id = id;
    }

    public String getShipmentId() {
        return shipmentId;
    }

    public void setShipmentId(String shipmentId) {
        this.shipmentId = shipmentId;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getAssetName() {
        return assetName;
    }

    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress1() {
        return address1;
    }

    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    public String getAddress2() {
        return address2;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    public String getAddress3() {
        return address3;
    }

    public void setAddress3(String address3) {
        this.address3 = address3;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public Double getShipmentValue() {
        return shipmentValue;
    }

    public void setShipmentValue(Double shipmentValue) {
        this.shipmentValue = shipmentValue;
    }

    public String getCustomerReferenceId() {
        return customerReferenceId;
    }

    public void setCustomerReferenceId(String customerReferenceId) {
        this.customerReferenceId = customerReferenceId;
    }

    public String getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(String createdOn) {
        this.createdOn = createdOn;
    }

    public String getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(String updatedOn) {
        this.updatedOn = updatedOn;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
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

    public Long getMposPaymentId() {
        return mposPaymentId;
    }

    public void setMposPaymentId(Long mposPaymentId) {
        this.mposPaymentId = mposPaymentId;
    }

    public String getLbn() {
        return lbn;
    }

    public void setLbn(String lbn) {
        this.lbn = lbn;
    }

    public String getCurrentBin() {
        return currentBin;
    }

    public void setCurrentBin(String currentBin) {
        this.currentBin = currentBin;
    }

    public String getCommittedExpectedDeliveryDate() {
        return committedExpectedDeliveryDate;
    }

    public void setCommittedExpectedDeliveryDate(String committedExpectedDeliveryDate) {
        this.committedExpectedDeliveryDate = committedExpectedDeliveryDate;
    }

    public Boolean getPartialDelivery() {
        return partialDelivery;
    }

    public void setPartialDelivery(Boolean partialDelivery) {
        this.partialDelivery = partialDelivery;
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

    public Boolean getProcessingBlocked() {
        return processingBlocked;
    }

    public void setProcessingBlocked(Boolean processingBlocked) {
        this.processingBlocked = processingBlocked;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getConsumerType() {
        return consumerType;
    }

    public void setConsumerType(String consumerType) {
        this.consumerType = consumerType;
    }

    public String getClientCode() {
        return clientCode;
    }

    public void setClientCode(String clientCode) {
        this.clientCode = clientCode;
    }

    public String getShipmentType() {
        return shipmentType;
    }

    public void setShipmentType(String shipmentType) {
        this.shipmentType = shipmentType;
    }

    public String getAllowPaymentMode() {
        return allowPaymentMode;
    }

    public void setAllowPaymentMode(String allowPaymentMode) {
        this.allowPaymentMode = allowPaymentMode;
    }

    public String getPriorityType() {
        return priorityType;
    }

    public void setPriorityType(String priorityType) {
        this.priorityType = priorityType;
    }

    public Boolean getAllowCheckPayment() {
        return allowCheckPayment;
    }

    public void setAllowCheckPayment(Boolean allowCheckPayment) {
        this.allowCheckPayment = allowCheckPayment;
    }

    public String getPackageId() {
        return packageId;
    }

    public void setPackageId(String packageId) {
        this.packageId = packageId;
    }

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public String getAdhocCreateType() {
        return adhocCreateType;
    }

    public void setAdhocCreateType(String adhocCreateType) {
        this.adhocCreateType = adhocCreateType;
    }

    public String getZoneName() {
        return zoneName;
    }

    public void setZoneName(String zoneName) {
        this.zoneName = zoneName;
    }

    public Integer getZoneId() {
        return zoneId;
    }

    public void setZoneId(Integer zoneId) {
        this.zoneId = zoneId;
    }

    public String getPickupPincode() {
        return pickupPincode;
    }

    public void setPickupPincode(String pickupPincode) {
        this.pickupPincode = pickupPincode;
    }

    public String getDropPincode() {
        return dropPincode;
    }

    public void setDropPincode(String dropPincode) {
        this.dropPincode = dropPincode;
    }

    public Boolean getSelected() {
        if (selected == null)
            return false;
        else
            return selected;
    }

    public void setSelected(Boolean selected) {
        this.selected = selected;
    }

    public Integer getMpsCount() {
        return mpsCount;
    }

    public void setMpsCount(Integer mpsCount) {
        this.mpsCount = mpsCount;
    }

    public String getParentShipment() {
        return parentShipment;
    }

    public void setParentShipment(String parentShipment) {
        this.parentShipment = parentShipment;
    }

    public Integer getFmExpectedCount() {
        return fmExpectedCount;
    }

    public void setFmExpectedCount(Integer fmExpectedCount) {
        this.fmExpectedCount = fmExpectedCount;
    }

    public Calendar getCustomerDefinedSlotStartTime() {
        return customerDefinedSlotStartTime;
    }

    public void setCustomerDefinedSlotStartTime(Calendar customerDefinedSlotStartTime) {
        this.customerDefinedSlotStartTime = customerDefinedSlotStartTime;
    }

    public Calendar getCustomerDefinedSlotEndTime() {
        return customerDefinedSlotEndTime;
    }

    public void setCustomerDefinedSlotEndTime(Calendar customerDefinedSlotEndTime) {
        this.customerDefinedSlotEndTime = customerDefinedSlotEndTime;
    }

    public ShipmentMetaDetails getShipmentMetaDetails() {
        return shipmentMetaDetails;
    }

    public void setShipmentMetaDetails(ShipmentMetaDetails shipmentMetaDetails) {
        this.shipmentMetaDetails = shipmentMetaDetails;
    }

    public static Creator<UnassignedDTO> getCREATOR() {
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
        return "UnassignedDTO{" +
                "id=" + id +
                ", shipmentId='" + shipmentId + '\'' +
                ", referenceId='" + referenceId + '\'' +
                ", orderId='" + orderId + '\'' +
                ", assetName='" + assetName + '\'' +
                ", name='" + name + '\'' +
                ", address1='" + address1 + '\'' +
                ", address2='" + address2 + '\'' +
                ", address3='" + address3 + '\'' +
                ", city='" + city + '\'' +
                ", state='" + state + '\'' +
                ", pincode='" + pincode + '\'' +
                ", paymentType='" + paymentType + '\'' +
                ", shipmentValue=" + shipmentValue +
                ", customerReferenceId='" + customerReferenceId + '\'' +
                ", createdOn='" + createdOn + '\'' +
                ", updatedOn='" + updatedOn + '\'' +
                ", items=" + items +
                ", type='" + type + '\'' +
                ", status='" + status + '\'' +
                ", mposPaymentId=" + mposPaymentId +
                ", lbn='" + lbn + '\'' +
                ", currentBin='" + currentBin + '\'' +
                ", committedExpectedDeliveryDate='" + committedExpectedDeliveryDate + '\'' +
                ", partialDelivery=" + partialDelivery +
                ", postponedToDate='" + postponedToDate + '\'' +
                ", slotStart='" + slotStart + '\'' +
                ", slotEnd='" + slotEnd + '\'' +
                ", processingBlocked=" + processingBlocked +
                ", serviceType='" + serviceType + '\'' +
                ", consumerType='" + consumerType + '\'' +
                ", clientCode='" + clientCode + '\'' +
                ", shipmentType='" + shipmentType + '\'' +
                ", allowPaymentMode='" + allowPaymentMode + '\'' +
                ", priorityType='" + priorityType + '\'' +
                ", allowCheckPayment=" + allowCheckPayment +
                ", packageId='" + packageId + '\'' +
                ", routeId='" + routeId + '\'' +
                ", adhocCreateType='" + adhocCreateType + '\'' +
                ", zoneName='" + zoneName + '\'' +
                ", zoneId=" + zoneId +
                ", selected=" + selected +
                '}';
    }
}
