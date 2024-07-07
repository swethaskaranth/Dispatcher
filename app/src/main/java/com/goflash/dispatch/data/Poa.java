package com.goflash.dispatch.data;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.goflash.dispatch.type.PoaType;

import co.uk.rushorm.core.Rush;
import co.uk.rushorm.core.RushCallback;
import co.uk.rushorm.core.RushCore;

public class Poa implements Parcelable, Rush {
    private String type;
    private boolean isRequired;
    private String description;

    private String regex;

    public Poa(){   }

    public Poa(String type, boolean isRequired, String description, String regex) {
        this.type = type;
        this.isRequired = isRequired;
        this.description = description;
        this.regex = regex;
    }

    public Poa(Parcel in){
        type = in.readString();
        isRequired = in.readByte() != 0;
        description = in.readString();
        regex = in.readString();
    }

    public static final Creator<Poa> CREATOR = new Creator<Poa>() {
        @Override
        public Poa createFromParcel(Parcel in) {
            return new Poa(in);
        }

        @Override
        public Poa[] newArray(int size) {
            return new Poa[size];
        }
    };

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isRequired() {
        return isRequired;
    }

    public void setRequired(boolean required) {
        isRequired = required;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(type);
        parcel.writeByte((byte) (isRequired?1:0));
        parcel.writeString(description);
        parcel.writeString(regex);
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
