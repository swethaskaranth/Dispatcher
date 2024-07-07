package com.goflash.dispatch.data;

import android.os.Parcel;
import android.os.Parcelable;

import co.uk.rushorm.core.Rush;
import co.uk.rushorm.core.RushCallback;
import co.uk.rushorm.core.RushCore;

public final class ExpenseDTO  implements Parcelable, Rush {

    String voucherFileUrl;
    String expenseType;
    String voucherNumber;
    Long amount;
    boolean invalid;
    boolean added;
    String fileType;

    public ExpenseDTO(){

    }

    public ExpenseDTO(String voucherFileUrl, String expenseType, String voucherNumber, Long amount, Boolean added, String fileType) {
        this.voucherFileUrl = voucherFileUrl;
        this.expenseType = expenseType;
        this.voucherNumber = voucherNumber;
        this.amount = amount;
        this.invalid = false;
        this.added = added;
        this.fileType = fileType;
    }

    protected ExpenseDTO(Parcel in) {
        voucherFileUrl = in.readString();
        expenseType = in.readString();
        voucherNumber = in.readString();
        if (in.readByte() == 0) {
            amount = null;
        } else {
            amount = in.readLong();
        }
        invalid = in.readByte() != 0;
        added = in.readByte() != 0;
        fileType = in.readString();
    }

    public static final Creator<ExpenseDTO> CREATOR = new Creator<ExpenseDTO>() {
        @Override
        public ExpenseDTO createFromParcel(Parcel in) {
            return new ExpenseDTO(in);
        }

        @Override
        public ExpenseDTO[] newArray(int size) {
            return new ExpenseDTO[size];
        }
    };

    public String getVoucherFileUrl() {
        return voucherFileUrl;
    }

    public void setVoucherFileUrl(String voucherFileUrl) {
        this.voucherFileUrl = voucherFileUrl;
    }

    public String getExpenseType() {
        return expenseType;
    }

    public void setExpenseType(String expenseType) {
        this.expenseType = expenseType;
    }

    public String getVoucherNumber() {
        return voucherNumber;
    }

    public void setVoucherNumber(String voucherNumber) {
        this.voucherNumber = voucherNumber;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public boolean isInvalid() {
        return invalid;
    }

    public void setInvalid(boolean invalid) {
        this.invalid = invalid;
    }

    public boolean isAdded() {
        return added;
    }

    public void setAdded(boolean added) {
        this.added = added;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(voucherFileUrl);
        parcel.writeString(expenseType);
        parcel.writeString(voucherNumber);
        parcel.writeLong(amount);
        parcel.writeByte((byte) (invalid? 1 : 0));
        parcel.writeByte((byte) (added? 1 : 0));
        parcel.writeString(fileType);
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
}
