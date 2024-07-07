package com.goflash.dispatch.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

import co.uk.rushorm.core.Rush;
import co.uk.rushorm.core.RushCallback;
import co.uk.rushorm.core.RushCore;

public class ReturnReconImageDTO implements Parcelable, Rush {

    private List<String> paths;

    public ReturnReconImageDTO(){

    }

    public ReturnReconImageDTO(List<String> paths) {
        this.paths = paths;
    }

    public List<String> getPaths() {
        return paths;
    }

    public void setPaths(List<String> paths) {
        this.paths = paths;
    }



    protected ReturnReconImageDTO(Parcel in) {
        paths = in.createStringArrayList();
    }

    public static final Creator<ReconImageDTO> CREATOR = new Creator<ReconImageDTO>() {
        @Override
        public ReconImageDTO createFromParcel(Parcel in) {
            return new ReconImageDTO(in);
        }

        @Override
        public ReconImageDTO[] newArray(int size) {
            return new ReconImageDTO[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeStringList(paths);
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
