package com.goflash.dispatch.data;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import co.uk.rushorm.core.Rush;
import co.uk.rushorm.core.RushCallback;
import co.uk.rushorm.core.RushCore;
import co.uk.rushorm.core.annotations.RushList;

public class TaskListDTO implements Rush, Parcelable {

    private Integer taskId;

    private String status;

    private String shipmentId;

    private String type;

    private String name = "";

    private String email;

    private String contact;

    private String address1;

    private String address2 = "";

    private String address3;

    private String city;

    private String state;

    private Integer pincode;

    private String addressId;

    private String paymentType;

    private String createdOn;

    private String updatedOn;

    private Integer priority;

    private Double shipmentValue;

    private String orderId;

    private String referenceId;

    private String lbn;

    private String packageId;

    private String addressType;

    private String actualPaymentType;

    private boolean partialPossible;

    private boolean isScanned;

    private String slotStart;

    private String slotEnd;

    private String priorityType;

    private Boolean acknowledgementRequired = false;

    private List<String> podRequiredPaymentModes = new ArrayList<>();

    private List<String> allowPaymentMode = new ArrayList<>();

    private Boolean allowCheckPayment = true;

    private Boolean partialDelivered = false;

    private String tripId;

    private List<ChildShipmentDTO> childShipments;

    private Calendar customerDefinedSlotStartTime;
    private Calendar customerDefinedSlotEndTime;

    public TaskListDTO() {

    }

    protected TaskListDTO(Parcel in) {
        if (in.readByte() == 0) {
            taskId = null;
        } else {
            taskId = in.readInt();
        }
        status = in.readString();
        shipmentId = in.readString();
        type = in.readString();
        name = in.readString();
        email = in.readString();
        contact = in.readString();
        address1 = in.readString();
        address2 = in.readString();
        address3 = in.readString();
        city = in.readString();
        state = in.readString();
        if (in.readByte() == 0) {
            pincode = null;
        } else {
            pincode = in.readInt();
        }

        addressId = in.readString();
        paymentType = in.readString();
        createdOn = in.readString();
        updatedOn = in.readString();
        if (in.readByte() == 0) {
            priority = null;
        } else {
            priority = in.readInt();
        }
        if (in.readByte() == 0) {
            shipmentValue = null;
        } else {
            shipmentValue = in.readDouble();
        }
        orderId = in.readString();
        referenceId = in.readString();
        lbn = in.readString();
        packageId = in.readString();
        addressType = in.readString();
        actualPaymentType = in.readString();
        partialPossible = in.readByte() != 0;
        isScanned = in.readByte() != 0;
        slotStart = in.readString();
        slotEnd = in.readString();
        priorityType = in.readString();
        byte tmpAcknowledgementRequired = in.readByte();
        acknowledgementRequired = tmpAcknowledgementRequired == 0 ? null : tmpAcknowledgementRequired == 1;
        podRequiredPaymentModes = in.createStringArrayList();
        allowPaymentMode = in.createStringArrayList();
        byte tmpAllowCheckPayment = in.readByte();
        allowCheckPayment = tmpAllowCheckPayment == 0 ? null : tmpAllowCheckPayment == 1;
        byte tmpPartialDelivered = in.readByte();
        partialDelivered = tmpPartialDelivered == 0 ? null : tmpPartialDelivered == 1;
        tripId = in.readString();

    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (taskId == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(taskId);
        }
        dest.writeString(status);
        dest.writeString(shipmentId);
        dest.writeString(type);
        dest.writeString(name);
        dest.writeString(email);
        dest.writeString(contact);
        dest.writeString(address1);
        dest.writeString(address2);
        dest.writeString(address3);
        dest.writeString(city);
        dest.writeString(state);
        if (pincode == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(pincode);
        }
        dest.writeString(addressId);
        dest.writeString(paymentType);
        dest.writeString(createdOn);
        dest.writeString(updatedOn);
        if (priority == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(priority);
        }
        if (shipmentValue == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(shipmentValue);
        }
        dest.writeString(orderId);
        dest.writeString(referenceId);
        dest.writeString(lbn);
        dest.writeString(packageId);
        dest.writeString(addressType);
        dest.writeString(actualPaymentType);
        dest.writeByte((byte) (partialPossible ? 1 : 0));
        dest.writeByte((byte) (isScanned ? 1 : 0));
        dest.writeString(slotStart);
        dest.writeString(slotEnd);
        dest.writeString(priorityType);
        dest.writeByte((byte) (acknowledgementRequired == null ? 0 : acknowledgementRequired ? 1 : 2));
        dest.writeStringList(podRequiredPaymentModes);
        dest.writeStringList(allowPaymentMode);
        dest.writeByte((byte) (allowCheckPayment == null ? 0 : allowCheckPayment ? 1 : 2));
        dest.writeByte((byte) (partialDelivered == null ? 0 : partialDelivered ? 1 : 2));
        dest.writeString(tripId);

    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<TaskListDTO> CREATOR = new Creator<TaskListDTO>() {
        @Override
        public TaskListDTO createFromParcel(Parcel in) {
            return new TaskListDTO(in);
        }

        @Override
        public TaskListDTO[] newArray(int size) {
            return new TaskListDTO[size];
        }
    };

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

    public Integer getTaskId() {
        return taskId;
    }

    public void setTaskId(Integer taskId) {
        this.taskId = taskId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getShipmentId() {
        return shipmentId;
    }

    public void setShipmentId(String shipmentId) {
        this.shipmentId = shipmentId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
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

    public Integer getPincode() {
        return pincode;
    }

    public void setPincode(Integer pincode) {
        this.pincode = pincode;
    }

    public String getAddressId() {
        return addressId;
    }

    public void setAddressId(String addressId) {
        this.addressId = addressId;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
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

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Double getShipmentValue() {
        return shipmentValue;
    }

    public void setShipmentValue(Double shipmentValue) {
        this.shipmentValue = shipmentValue;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setAddressType(String addressType) {
        this.addressType = addressType;
    }

    public String getAddressType() {
        return addressType;
    }

    public String getActualPaymentType() {
        return actualPaymentType;
    }

    public void setActualPaymentType(String actualPaymentType) {
        this.actualPaymentType = actualPaymentType;
    }

    public boolean getPartialPossible() {
        return partialPossible;
    }

    public void setPartialPossible(boolean partialPossible) {
        this.partialPossible = partialPossible;
    }

    public boolean isPartialPossible() {
        return partialPossible;
    }

    public boolean isScanned() {
        return isScanned;
    }

    public void setScanned(boolean scanned) {
        isScanned = scanned;
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

    public String getPriorityType() {
        return priorityType;
    }

    public void setPriorityType(String priorityType) {
        this.priorityType = priorityType;
    }

    public Boolean getAcknowledgementRequired() {
        return acknowledgementRequired;
    }

    public void setAcknowledgementRequired(Boolean b2bFlow) {
        this.acknowledgementRequired = b2bFlow;
    }

    public List<String> getPodRequiredPaymentModes() {
        List<String> trimmedStrings = new ArrayList<String>();
        for (String s : podRequiredPaymentModes) {
            trimmedStrings.add(s.trim());
        }

        podRequiredPaymentModes = trimmedStrings;
        return podRequiredPaymentModes;
    }

    public void setPodRequiredPaymentModes(List<String> podRequiredPaymentModes) {
        this.podRequiredPaymentModes = podRequiredPaymentModes;
    }

    public List<String> getAllowPaymentMode() {
        List<String> trimmedStrings = new ArrayList<String>();
        for (String s : allowPaymentMode) {
            trimmedStrings.add(s.trim());
        }

        allowPaymentMode = trimmedStrings;
        return allowPaymentMode;
    }

    public void setAllowPaymentMode(List<String> allowPaymentMode) {
        this.allowPaymentMode = allowPaymentMode;
    }

    public Boolean getAllowCheckPayment() {
        return allowCheckPayment;
    }

    public void setAllowCheckPayment(Boolean allowCheckPayment) {
        this.allowCheckPayment = allowCheckPayment;
    }

    public Boolean getPartialDelivered() {
        return partialDelivered;
    }

    public void setPartialDelivered(Boolean partialDelivered) {
        this.partialDelivered = partialDelivered;
    }

    public String getLbn() {
        return lbn;
    }

    public void setLbn(String lbn) {
        this.lbn = lbn;
    }

    public String getPackageId() {
        return packageId;
    }

    public void setPackageId(String packageId) {
        this.packageId = packageId;
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public List<ChildShipmentDTO> getChildShipments() {
        return childShipments;
    }

    public void setChildShipments(List<ChildShipmentDTO> childShipments) {
        this.childShipments = childShipments;
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

    @Override
    public String toString() {
        return "TaskListDTO{" +
                "taskId=" + taskId +
                ", status='" + status + '\'' +
                ", shipmentId='" + shipmentId + '\'' +
                ", type='" + type + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", contact='" + contact + '\'' +
                ", address1='" + address1 + '\'' +
                ", address2='" + address2 + '\'' +
                ", address3='" + address3 + '\'' +
                ", city='" + city + '\'' +
                ", state='" + state + '\'' +
                ", pincode=" + pincode +
                ", addressId='" + addressId + '\'' +
                ", paymentType='" + paymentType + '\'' +
                ", createdOn='" + createdOn + '\'' +
                ", updatedOn='" + updatedOn + '\'' +
                ", priority=" + priority +
                ", shipmentValue=" + shipmentValue +
                ", orderId='" + orderId + '\'' +
                ", referenceId='" + referenceId + '\'' +
                ", lbn='" + lbn + '\'' +
                ", packageId='" + packageId + '\'' +
                ", addressType='" + addressType + '\'' +
                ", actualPaymentType='" + actualPaymentType + '\'' +
                ", partialPossible=" + partialPossible +
                ", isScanned=" + isScanned +
                ", slotStart='" + slotStart + '\'' +
                ", slotEnd='" + slotEnd + '\'' +
                ", priorityType='" + priorityType + '\'' +
                ", acknowledgementRequired=" + acknowledgementRequired +
                ", podRequiredPaymentModes=" + podRequiredPaymentModes +
                ", allowPaymentMode=" + allowPaymentMode +
                ", allowCheckPayment=" + allowCheckPayment +
                ", partialDelivered=" + partialDelivered +
                ", tripId='" + tripId + '\'' +
                ", childShipments=" + childShipments +
                '}';
    }
}
