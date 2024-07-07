package com.goflash.dispatch.data;

import android.os.Parcel;
import android.os.Parcelable;

import co.uk.rushorm.core.Rush;
import co.uk.rushorm.core.RushCallback;
import co.uk.rushorm.core.RushCore;

public class CashPickupDTO implements Parcelable, Rush {

    Long one;
    Long two;
    Long five;
    Long ten;
    Long twenty;
    Long fifty;
    Long hundred;
    Long twoHundred;
    Long fiveHundred;
    Long twoThousand;
    String cashDepositFileUrl;
    String cashDepositReceiptNumber;
    String atmId;
    String depositType;

    public CashPickupDTO() {
    }

    public CashPickupDTO(Long one, Long two, Long five, Long ten, Long twenty, Long fifty, Long hundred, Long twoHundred, Long fiveHundred, Long twoThousand, String cashDepositFileUrl, String cashDepositReceiptNumber, String atmId, String depositType
    ) {
        this.one = one;
        this.two = two;
        this.five = five;
        this.ten = ten;
        this.twenty = twenty;
        this.fifty = fifty;
        this.hundred = hundred;
        this.twoHundred = twoHundred;
        this.fiveHundred = fiveHundred;
        this.twoThousand = twoThousand;
        this.cashDepositFileUrl = cashDepositFileUrl;
        this.cashDepositReceiptNumber = cashDepositReceiptNumber;
        this.atmId = atmId;
        this.depositType = depositType;
    }

    protected CashPickupDTO(Parcel in) {
        if (in.readByte() == 0) {
            one = null;
        } else {
            one = in.readLong();
        }
        if (in.readByte() == 0) {
            two = null;
        } else {
            two = in.readLong();
        }
        if (in.readByte() == 0) {
            five = null;
        } else {
            five = in.readLong();
        }
        if (in.readByte() == 0) {
            ten = null;
        } else {
            ten = in.readLong();
        }
        if (in.readByte() == 0) {
            twenty = null;
        } else {
            twenty = in.readLong();
        }
        if (in.readByte() == 0) {
            fifty = null;
        } else {
            fifty = in.readLong();
        }
        if (in.readByte() == 0) {
            hundred = null;
        } else {
            hundred = in.readLong();
        }
        if (in.readByte() == 0) {
            twoHundred = null;
        } else {
            twoHundred = in.readLong();
        }
        if (in.readByte() == 0) {
            fiveHundred = null;
        } else {
            fiveHundred = in.readLong();
        }
        if (in.readByte() == 0) {
            twoThousand = null;
        } else {
            twoThousand = in.readLong();
        }
        cashDepositFileUrl = in.readString();
        cashDepositReceiptNumber = in.readString();
        atmId = in.readString();
        depositType = in.readString();
    }

    public static final Creator<CashPickupDTO> CREATOR = new Creator<CashPickupDTO>() {
        @Override
        public CashPickupDTO createFromParcel(Parcel in) {
            return new CashPickupDTO(in);
        }

        @Override
        public CashPickupDTO[] newArray(int size) {
            return new CashPickupDTO[size];
        }
    };

    public Long getOne() {
        return one;
    }

    public void setOne(Long one) {
        this.one = one;
    }

    public Long getTwo() {
        return two;
    }

    public void setTwo(Long two) {
        this.two = two;
    }

    public Long getFive() {
        return five;
    }

    public void setFive(Long five) {
        this.five = five;
    }

    public Long getTen() {
        return ten;
    }

    public void setTen(Long ten) {
        this.ten = ten;
    }

    public Long getTwenty() {
        return twenty;
    }

    public void setTwenty(Long twenty) {
        this.twenty = twenty;
    }

    public Long getFifty() {
        return fifty;
    }

    public void setFifty(Long fifty) {
        this.fifty = fifty;
    }

    public Long getHundred() {
        return hundred;
    }

    public void setHundred(Long hundred) {
        this.hundred = hundred;
    }

    public Long getTwoHundred() {
        return twoHundred;
    }

    public void setTwoHundred(Long twoHundred) {
        this.twoHundred = twoHundred;
    }

    public Long getFiveHundred() {
        return fiveHundred;
    }

    public void setFiveHundred(Long fiveHundred) {
        this.fiveHundred = fiveHundred;
    }

    public Long getTwoThousand() {
        return twoThousand;
    }

    public void setTwoThousand(Long twoThousand) {
        this.twoThousand = twoThousand;
    }

    public String getCashDepositFileUrl() {
        return cashDepositFileUrl;
    }

    public void setCashDepositFileUrl(String cashDepositFileUrl) {
        this.cashDepositFileUrl = cashDepositFileUrl;
    }

    public String getCashDepositReceiptNumber() {
        return cashDepositReceiptNumber;
    }

    public void setCashDepositReceiptNumber(String cashDepositReceiptNumber) {
        this.cashDepositReceiptNumber = cashDepositReceiptNumber;
    }

    public String getAtmId() {
        return atmId;
    }

    public void setAtmId(String atmId) {
        this.atmId = atmId;
    }

    public String getDepositType() {
        return depositType;
    }

    public void setDepositType(String depositType) {
        this.depositType = depositType;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        if (one == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeLong(one);
        }
        if (two == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeLong(two);
        }
        if (five == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeLong(five);
        }
        if (ten == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeLong(ten);
        }
        if (twenty == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeLong(twenty);
        }
        if (fifty == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeLong(fifty);
        }
        if (hundred == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeLong(hundred);
        }
        if (twoHundred == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeLong(twoHundred);
        }
        if (fiveHundred == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeLong(fiveHundred);
        }
        if (twoThousand == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeLong(twoThousand);
        }
        parcel.writeString(cashDepositFileUrl);
        parcel.writeString(cashDepositReceiptNumber);
        parcel.writeString(atmId);
        parcel.writeString(depositType);
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
