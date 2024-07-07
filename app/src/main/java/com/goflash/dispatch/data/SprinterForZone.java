package com.goflash.dispatch.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

import co.uk.rushorm.core.Rush;
import co.uk.rushorm.core.RushCallback;
import co.uk.rushorm.core.RushCore;

public class SprinterForZone implements Parcelable, Rush {


    private  String id;

    private  String name;

    private boolean disabled;

    private boolean restricted;

    private String restrictionReason;

    public SprinterForZone() {

    }

    public SprinterForZone(String id, String name, boolean disabled, boolean restricted, String restrictionReason) {
        this.id = id;
        this.name = name;
        this.disabled = disabled;
        this.restricted = restricted;
        this.restrictionReason = restrictionReason;
    }

    protected SprinterForZone(Parcel in) {
        id = in.readString();
        name = in.readString();
        disabled = in.readByte() != 0;
        restricted = in.readByte() != 0;
        restrictionReason = in.readString();
    }

    public static final Creator<SprinterForZone> CREATOR = new Creator<SprinterForZone>() {
        @Override
        public SprinterForZone createFromParcel(Parcel in) {
            return new SprinterForZone(in);
        }

        @Override
        public SprinterForZone[] newArray(int size) {
            return new SprinterForZone[size];
        }
    };


    public String getSprinterId() {
        return id;
    }

    public void setSprinterId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public boolean isRestricted() {
        return restricted;
    }

    public void setRestricted(boolean restricted) {
        this.restricted = restricted;
    }

    public String getRestrictionReason() {
        return restrictionReason;
    }

    public void setRestrictionReason(String restrictionReason) {
        this.restrictionReason = restrictionReason;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeByte((byte) (disabled? 1 :0));
        dest.writeByte((byte) (restricted? 1 :0));
        dest.writeString(restrictionReason);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SprinterForZone that = (SprinterForZone) o;
        return id.equals(that.id);
    }

}
