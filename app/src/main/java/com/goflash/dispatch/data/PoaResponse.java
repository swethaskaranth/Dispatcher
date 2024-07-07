package com.goflash.dispatch.data;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.List;

import co.uk.rushorm.core.Rush;
import co.uk.rushorm.core.RushCallback;
import co.uk.rushorm.core.RushCore;
import co.uk.rushorm.core.annotations.RushList;

public class PoaResponse implements Parcelable, Rush {
    @RushList(classType = Poa.class)
    private List<Poa> mandatoryPoa;

    @RushList(classType = Poa.class)
    private List<Poa> optionalPoa;

    private int minOptional = 0;

    public PoaResponse(){

    }

    public PoaResponse(List<Poa> mandatoryPoa, List<Poa> optionalPoa, int minOptional) {
        this.mandatoryPoa = mandatoryPoa;
        this.optionalPoa = optionalPoa;
        this.minOptional = minOptional;
    }

    public PoaResponse(Parcel in){
        mandatoryPoa = in.createTypedArrayList(Poa.CREATOR);
        optionalPoa = in.createTypedArrayList(Poa.CREATOR);
        minOptional = in.readInt();
    }

    public static final Creator<PoaResponse> CREATOR = new Creator<PoaResponse>() {
        @Override
        public PoaResponse createFromParcel(Parcel in) {
            return new PoaResponse(in);
        }

        @Override
        public PoaResponse[] newArray(int size) {
            return new PoaResponse[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeTypedList(mandatoryPoa);
        parcel.writeTypedList(optionalPoa);
        parcel.writeInt(minOptional);
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

    public List<Poa> getMandatoryPoa() {
        return mandatoryPoa;
    }

    public void setMandatoryPoa(List<Poa> mandatoryPoa) {
        this.mandatoryPoa = mandatoryPoa;
    }

    public List<Poa> getOptionalPoa() {
        return optionalPoa;
    }

    public void setOptionalPoa(List<Poa> optionalPoa) {
        this.optionalPoa = optionalPoa;
    }

    public int getMinOptional() {
        return minOptional;
    }

    public void setMinOptional(int minOptional) {
        this.minOptional = minOptional;
    }
}
