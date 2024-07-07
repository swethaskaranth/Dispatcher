package com.goflash.dispatch.data;

import android.os.Parcel;
import android.os.Parcelable;

import org.jetbrains.annotations.NotNull;

import co.uk.rushorm.core.Rush;
import co.uk.rushorm.core.RushCallback;
import co.uk.rushorm.core.RushCore;

/**
 * Created by Ravi on 2020-06-18.
 */
public class CashChequeCollectedDetailsDTO implements Parcelable, Rush {

    private String shipmentId;
    private String lbn;
    private String referenceId;
    private String packageId;
    private String customerName;
    private Double amount;
    private String transactionId;
    private String paymentType;

    public CashChequeCollectedDetailsDTO(){}

    public CashChequeCollectedDetailsDTO(String shipmentId, String lbn, String referenceId,
                                         String packageId, String customerName, Double amount,
                                         String transactionId, String paymentType) {
        this.shipmentId = shipmentId;
        this.lbn = lbn;
        this.referenceId = referenceId;
        this.packageId = packageId;
        this.customerName = customerName;
        this.amount = amount;
        this.transactionId = transactionId;
        this.paymentType = paymentType;
    }

    protected CashChequeCollectedDetailsDTO(Parcel in) {
        shipmentId = in.readString();
        lbn = in.readString();
        referenceId = in.readString();
        packageId = in.readString();
        customerName = in.readString();
        transactionId = in.readString();
        paymentType = in.readString();
        if (in.readByte() == 0) {
            amount = null;
        } else {
            amount = in.readDouble();
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(shipmentId);
        dest.writeString(lbn);
        dest.writeString(referenceId);
        dest.writeString(packageId);
        dest.writeString(customerName);
        dest.writeString(transactionId);
        dest.writeString(paymentType);
        if (amount == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(amount);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<CashChequeCollectedDetailsDTO> CREATOR = new Creator<CashChequeCollectedDetailsDTO>() {
        @Override
        public CashChequeCollectedDetailsDTO createFromParcel(Parcel in) {
            return new CashChequeCollectedDetailsDTO(in);
        }

        @Override
        public CashChequeCollectedDetailsDTO[] newArray(int size) {
            return new CashChequeCollectedDetailsDTO[size];
        }
    };

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

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
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

    @NotNull
    @Override
    public String toString() {
        return "CashChequeCollectedDetailsDTO{" +
                "shipmentId='" + shipmentId + '\'' +
                ", lbn='" + lbn + '\'' +
                ", referenceId='" + referenceId + '\'' +
                ", packageId='" + packageId + '\'' +
                ", customerName='" + customerName + '\'' +
                ", amount=" + amount +
                ", transactionId=" + transactionId +
                ", paymentType=" + paymentType +
                '}';
    }
}
