package com.goflash.dispatch.data;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.List;
import co.uk.rushorm.core.Rush;
import co.uk.rushorm.core.RushCallback;
import co.uk.rushorm.core.RushCore;
import co.uk.rushorm.core.annotations.RushList;

/**
 * Created by Ravi on 2020-06-18.
 */
public class TripList implements Parcelable, Rush {

    @RushList(classType = TripDTO.class)
    private List<TripDTO> taskDTO;

    public TripList(List<TripDTO> taskDTO) {
        this.taskDTO = taskDTO;
    }

    public List<TripDTO> getTaskDTO() {
        return taskDTO;
    }

    public void setTaskDTO(List<TripDTO> taskDTO) {
        this.taskDTO = taskDTO;
    }

    public static Creator<TripList> getCREATOR() {
        return CREATOR;
    }

    protected TripList(Parcel in) {
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<TripList> CREATOR = new Creator<TripList>() {
        @Override
        public TripList createFromParcel(Parcel in) {
            return new TripList(in);
        }

        @Override
        public TripList[] newArray(int size) {
            return new TripList[size];
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

    @Override
    public String toString() {
        return "TripList{" +
                "taskDTO=" + taskDTO +
                '}';
    }
}
