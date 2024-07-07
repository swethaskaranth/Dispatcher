package com.goflash.dispatch.data;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;
import co.uk.rushorm.core.Rush;
import co.uk.rushorm.core.RushCallback;
import co.uk.rushorm.core.RushCore;

/**
 * Created by Ravi on 2019-09-16.
 */
public class ReceivingDto implements Serializable, Rush {

    @SerializedName("id")
    public long tripId;
    private String status;
    private List<Tasks> tasks;
    private String agentName;
    private String agentPhone;
    private String assetName;
    private String vehicleId;
    private String createdOn;

    public void save() {
        RushCore.getInstance().saveOnlyWithoutConflict(this);
    }

    public void save( RushCallback callback) {
        RushCore.getInstance().save(this, callback);
    }

    public void delete() {
        RushCore.getInstance().delete(this);
    }

    public void delete( RushCallback callback) {
        RushCore.getInstance().delete(this, callback);
    }

    @Override
    public String getId() {
        return RushCore.getInstance().getId(this);
    }

    public long getTripId() {
        return tripId;
    }

    public void setTripId(long tripId) {
        this.tripId = tripId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<Tasks> getTasks() {
        return tasks;
    }

    public void setTasks(List<Tasks> tasks) {
        this.tasks = tasks;
    }

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    public String getAgentPhone() {
        return agentPhone;
    }

    public void setAgentPhone(String agentPhone) {
        this.agentPhone = agentPhone;
    }

    public String getAssetName() {
        return assetName;
    }

    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public ReceivingDto(long tripId, String status, List<Tasks> tasks, String agentName, String agentPhone, String assetName, String vehicleId, String createdOn) {
        this.tripId = tripId;
        this.status = status;
        this.tasks = tasks;
        this.agentName = agentName;
        this.agentPhone = agentPhone;
        this.assetName = assetName;
        this.vehicleId = vehicleId;
        this.createdOn = createdOn;
    }

    public ReceivingDto() {

    }

    public String getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(String createdOn) {
        this.createdOn = createdOn;
    }


}
