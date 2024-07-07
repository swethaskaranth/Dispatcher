package com.goflash.dispatch.data;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import co.uk.rushorm.core.Rush;
import co.uk.rushorm.core.RushCallback;
import co.uk.rushorm.core.RushCore;

public class PackageCountDTO implements Parcelable, Rush {


    private int cases;
    private int polyBags;
    private int icePacks;
    private int total;

    public PackageCountDTO(){

    }

    public PackageCountDTO(int cases, int polyBags, int icePacks, int total) {
        this.cases = cases;
        this.polyBags = polyBags;
        this.icePacks = icePacks;
        this.total = total;
    }

    public int getCases() {
        return cases;
    }

    public void setCases(int cases) {
        this.cases = cases;
    }

    public int getPolyBags() {
        return polyBags;
    }

    public void setPolyBags(int polyBags) {
        this.polyBags = polyBags;
    }

    public int getIcePacks() {
        return icePacks;
    }

    public void setIcePacks(int icePacks) {
        this.icePacks = icePacks;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeInt(cases);
        parcel.writeInt(polyBags);
        parcel.writeInt(icePacks);
        parcel.writeInt(total);


    }

    public PackageCountDTO(Parcel in){
        cases = in.readInt();
        polyBags = in.readInt();
        icePacks = in.readInt();
        total = in.readInt();

    }

    public static final Creator<PackageCountDTO> CREATOR = new Creator<PackageCountDTO>() {
        @Override
        public PackageCountDTO createFromParcel(Parcel in) {
            return new PackageCountDTO(in);
        }

        @Override
        public PackageCountDTO[] newArray(int size) {
            return new PackageCountDTO[size];
        }
    };

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
