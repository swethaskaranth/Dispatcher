package com.goflash.dispatch.data;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import co.uk.rushorm.core.Rush;
import co.uk.rushorm.core.RushCallback;
import co.uk.rushorm.core.RushCore;

public class MetaDetails implements Parcelable, Rush {

    private PackageCountDTO packageCounts;


    public MetaDetails(){

    }

    public MetaDetails(PackageCountDTO packageCounts) {
        this.packageCounts = packageCounts;
    }

    public MetaDetails(Parcel parcel){
        packageCounts = parcel.readParcelable(PackageCountDTO.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeParcelable(packageCounts,i);
    }

    public PackageCountDTO getPackageCounts() {
        return packageCounts;
    }

    public void setPackageCounts(PackageCountDTO packageCounts) {
        this.packageCounts = packageCounts;
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
