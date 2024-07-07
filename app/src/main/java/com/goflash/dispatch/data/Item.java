package com.goflash.dispatch.data;

import android.os.Parcel;
import android.os.Parcelable;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Date;

import co.uk.rushorm.core.Rush;
import co.uk.rushorm.core.RushCallback;
import co.uk.rushorm.core.RushCore;

public class Item implements Rush, Parcelable {

    private Integer itemId;

    private String ucode;

    private String batchNumber;
    @Nullable
    private Boolean canBeReturned;
    @Nullable
    private String productCode;
    @NotNull
    private String displayName;
    @Nullable
    private Integer quantity;

    private String barcode;

    private boolean selected;

    private boolean itemScanned;

    private String returnReason;

    private int selectedQuantity;

    private Date updatedOn;

    private Boolean refrigerated;

    private Integer returnedQuantity;

    private String status;

    private Long productId;

    private Integer returnRaisedQuantity;

    private Integer pickedUpQuantity;

    private String reason;

    private String reconStatus;

    private String reconStatusReason;

    private Integer reconAcceptedQuantity;

    private Integer reconRejectedQuantity = 0;

    private String shipmentId;

    private String shipmentIdRef;

    private Integer acceptedQuantity;

    private Integer rejectedQuantiy;

    private boolean scanned;

    private String reconRemark;


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

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public String getUcode() {
        return ucode;
    }

    public void setUcode(String ucode) {
        this.ucode = ucode;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    @Nullable
    public Boolean getCanBeReturned() {
        return canBeReturned;
    }

    public void setCanBeReturned(@Nullable Boolean canBeReturned) {
        this.canBeReturned = canBeReturned;
    }

    @Nullable
    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(@Nullable String productCode) {
        this.productCode = productCode;
    }

    @NotNull
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(@NotNull String displayName) {
        this.displayName = displayName;
    }


    public Integer getQuantity() {
        if(quantity == null)
            quantity = 0;
        return quantity;
    }

    public void setQuantity(@Nullable Integer quantity) {
        this.quantity = quantity;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isItemScanned() {
        return itemScanned;
    }

    public void setItemScanned(boolean itemScanned) {
        this.itemScanned = itemScanned;
    }

    public String getReturnReason() {
        return returnReason;
    }

    public void setReturnReason(String returnReason) {
        this.returnReason = returnReason;
    }

    public int getSelectedQuantity() {
        return selectedQuantity;
    }

    public void setSelectedQuantity(int selectedQuantity) {
        this.selectedQuantity = selectedQuantity;
    }

    public Date getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(Date updatedOn) {
        this.updatedOn = updatedOn;
    }

    public Boolean getRefrigerated() {
        return refrigerated;
    }

    public void setRefrigerated(Boolean refrigerated) {
        this.refrigerated = refrigerated;
    }

    public Integer getReturnedQuantity() {
        if(returnedQuantity == null)
            returnedQuantity = 0;
        return returnedQuantity;
    }

    public void setReturnedQuantity(Integer returnedQuantity) {
        this.returnedQuantity = returnedQuantity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getReturnRaisedQuantity() {
        if(returnRaisedQuantity == null)
            returnRaisedQuantity=0;
        return returnRaisedQuantity;
    }

    public void setReturnRaisedQuantity(Integer returnRaisedQuantity) {
        this.returnRaisedQuantity = returnRaisedQuantity;
    }

    public Integer getPickedUpQuantity() {
        return pickedUpQuantity;
    }

    public void setPickedUpQuantity(Integer pickedUpQuantity) {
        this.pickedUpQuantity = pickedUpQuantity;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getReconStatus() {
        return reconStatus;
    }

    public void setReconStatus(String reconStatus) {
        this.reconStatus = reconStatus;
    }

    public String getReconStatusReason() {
        return reconStatusReason;
    }

    public void setReconStatusReason(String reconStatusReason) {
        this.reconStatusReason = reconStatusReason;
    }

    public Integer getReconAcceptedQuantity() {
        if(reconAcceptedQuantity == null)
            return 0;
        return reconAcceptedQuantity;
    }

    public void setReconAcceptedQuantity(Integer reconAcceptedQuantity) {
        this.reconAcceptedQuantity = reconAcceptedQuantity;
    }

    public Integer getReconRejectedQuantity() {
        if(reconRejectedQuantity == null)
            return 0;
        return reconRejectedQuantity;
    }

    public void setReconRejectedQuantity(Integer reconRejectedQuantity) {
        this.reconRejectedQuantity = reconRejectedQuantity;
    }

    public String getShipmentId() {
        return shipmentId;
    }

    public void setShipmentId(String shipmentId) {
        this.shipmentId = shipmentId;
    }

    public String getShipmentIdRef() {
        return shipmentIdRef;
    }

    public void setShipmentIdRef(String shipmentIdRef) {
        this.shipmentIdRef = shipmentIdRef;
    }

    public Integer getAcceptedQuantity() {

        if (acceptedQuantity == null) acceptedQuantity = 0;
        return acceptedQuantity;
    }

    public void setAcceptedQuantity(Integer acceptedQuantity) {
        this.acceptedQuantity = acceptedQuantity;
    }

    public Integer getRejectedQuantiy() {

        if (rejectedQuantiy == null) rejectedQuantiy = 0;
        return rejectedQuantiy;
    }

    public void setRejectedQuantiy(Integer rejectedQuantiy) {
        this.rejectedQuantiy = rejectedQuantiy;
    }

    public boolean isScanned() {
        return scanned;
    }

    public void setScanned(boolean scanned) {
        this.scanned = scanned;
    }

    public String getReconRemark() {
        return reconRemark;
    }

    public void setReconRemark(String reconRemark) {
        this.reconRemark = reconRemark;
    }

    public static final Creator<Item> CREATOR = new Creator<Item>() {
        @Override
        public Item createFromParcel(Parcel in) {
            return new Item(in);
        }

        @Override
        public Item[] newArray(int size) {
            return new Item[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(itemId);
        dest.writeString(ucode);
        dest.writeString(batchNumber);
        dest.writeByte((byte) (canBeReturned? 1 : 0));
        dest.writeString(productCode);
        dest.writeString(displayName);
        dest.writeInt(quantity);
        dest.writeString(barcode);
        dest.writeByte((byte) (selected? 1 : 0));
        dest.writeByte((byte) (itemScanned ? 1 : 0));
        dest.writeString(returnReason);
        dest.writeInt(selectedQuantity);
        dest.writeByte((byte) (refrigerated? 1 : 0));
        dest.writeInt(returnedQuantity);
        dest.writeString(status);
        dest.writeLong(productId);
        dest.writeInt(returnRaisedQuantity);
        dest.writeInt(pickedUpQuantity);
        dest.writeString(reason);
        dest.writeString(reconStatus);
        dest.writeString(reconStatusReason);
        dest.writeInt(reconAcceptedQuantity);
        dest.writeInt(reconRejectedQuantity);
        dest.writeString(shipmentId);
        dest.writeString(shipmentIdRef);
        dest.writeInt(acceptedQuantity);
        dest.writeInt(rejectedQuantiy);
        dest.writeByte((byte) (scanned? 1 : 0));
        dest.writeString(reconRemark);
    }

    public Item(Parcel in){
        this.itemId = in.readInt();
        this.ucode = in.readString();
        this.batchNumber = in.readString();
        this.canBeReturned = in.readByte() != 0;
        this.productCode = in.readString();
        this.displayName =  in.readString();;
        this.quantity = in.readInt();
        this.barcode =  in.readString();;
        this.selected = in.readByte() != 0;
        this.itemScanned = in.readByte() != 0;
        this.returnReason =  in.readString();;
        this.selectedQuantity = in.readInt();
        this.refrigerated = in.readByte() != 0;
        this.returnedQuantity = in.readInt();
        this.status = in.readString();
        this.productId =  in.readLong();;
        this.returnRaisedQuantity = in.readInt();
        this.pickedUpQuantity = in.readInt();
        this.reason =  in.readString();;
        this.reconStatus =  in.readString();;
        this.reconStatusReason =  in.readString();;
        this.reconAcceptedQuantity = in.readInt();
        this.reconRejectedQuantity = in.readInt();
        this.shipmentId =  in.readString();;
        this.shipmentIdRef =  in.readString();;
        this.acceptedQuantity = in.readInt();
        this.rejectedQuantiy = in.readInt();
        this.scanned = in.readByte() != 0;
        this.reconRemark = in.readString();
    }

    public Item() {

    }

    public Item(Integer itemId, String ucode, String batchNumber, @Nullable Boolean canBeReturned, @Nullable String productCode, @NotNull String displayName, @Nullable Integer quantity, String barcode, boolean selected, boolean itemScanned, String returnReason, int selectedQuantity, Date updatedOn, Boolean refrigerated, Integer returnedQuantity, String status, Long productId, Integer returnRaisedQuantity, Integer pickedUpQuantity, String reason, String reconStatus, String reconStatusReason, Integer reconAcceptedQuantity, Integer reconRejectedQuantity, String shipmentId, String shipmentIdRef, Integer acceptedQuantity, Integer rejectedQuantiy, boolean scanned, String reconRemark) {
        this.itemId = itemId;
        this.ucode = ucode;
        this.batchNumber = batchNumber;
        this.canBeReturned = canBeReturned;
        this.productCode = productCode;
        this.displayName = displayName;
        this.quantity = quantity;
        this.barcode = barcode;
        this.selected = selected;
        this.itemScanned = itemScanned;
        this.returnReason = returnReason;
        this.selectedQuantity = selectedQuantity;
        this.updatedOn = updatedOn;
        this.refrigerated = refrigerated;
        this.returnedQuantity = returnedQuantity;
        this.status = status;
        this.productId = productId;
        this.returnRaisedQuantity = returnRaisedQuantity;
        this.pickedUpQuantity = pickedUpQuantity;
        this.reason = reason;
        this.reconStatus = reconStatus;
        this.reconStatusReason = reconStatusReason;
        this.reconAcceptedQuantity = reconAcceptedQuantity;
        this.reconRejectedQuantity = reconRejectedQuantity;
        this.shipmentId = shipmentId;
        this.shipmentIdRef = shipmentIdRef;
        this.acceptedQuantity = acceptedQuantity;
        this.rejectedQuantiy = rejectedQuantiy;
        this.scanned = scanned;
        this.reconRemark = reconRemark;
    }

    @Override
    public String toString() {
        return "Item{" +
                "id=" + itemId +
                ", ucode='" + ucode + '\'' +
                ", batchNumber='" + batchNumber + '\'' +
                ", canBeReturned=" + canBeReturned +
                ", productCode='" + productCode + '\'' +
                ", displayName='" + displayName + '\'' +
                ", quantity=" + quantity +
                ", barcode='" + barcode + '\'' +
                ", selected=" + selected +
                ", scanned=" + itemScanned +
                ", returnReason='" + returnReason + '\'' +
                ", selectedQuantity=" + selectedQuantity +
                ", updatedOn=" + updatedOn +
                ", refrigerated=" + refrigerated +
                ", returnedQuantity=" + returnedQuantity +
                ", status='" + status + '\'' +
                ", productId=" + productId +
                ", returnRaisedQuantity=" + returnRaisedQuantity +
                ", pickedUpQuantity=" + pickedUpQuantity +
                ", reason='" + reason + '\'' +
                ", reconStatus='" + reconStatus + '\'' +
                ", reconAcceptedQuantity=" + reconAcceptedQuantity +
                ", reconRejectedQuantity=" + reconRejectedQuantity +
                ", shipmentId='" + shipmentId + '\'' +
                ", shipmentIdRef='" + shipmentIdRef + '\'' +
                '}';
    }
}
