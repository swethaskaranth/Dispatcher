package com.goflash.dispatch.data;

import java.io.Serializable;

import co.uk.rushorm.core.Rush;
import co.uk.rushorm.core.RushCallback;
import co.uk.rushorm.core.RushCore;

/**
 * Created by Ravi on 2019-09-16.
 */
public class VehicleDetails implements Serializable, Rush {

    private String bagId;
    private String sourceName;
    private String sourceId;
    private String destinationName;
    private String destinationId;
    private String currentNodeName;
    private Boolean canBePicked = false;
    private String vehicleId;
    private boolean isScanned;
    private String returnReason;
    private String tripId;

    public VehicleDetails(String bagId, String sourceName, String sourceId, String destinationName, String destinationId, String currentNode, Boolean canBePicked, String vehicleId, boolean isScanned, String returnReason, String tripId) {
        this.bagId = bagId;
        this.sourceName = sourceName;
        this.sourceId = sourceId;
        this.destinationName = destinationName;
        this.destinationId = destinationId;
        this.currentNodeName = currentNode;
        this.canBePicked = canBePicked;
        this.vehicleId = vehicleId;
        this.isScanned = isScanned;
        this.returnReason = returnReason;
        this.tripId = tripId;
    }

    public VehicleDetails(){

    }

    @Override
    public void save() {
        RushCore.getInstance().saveOnlyWithoutConflict(this);
    }

    @Override
    public void save( RushCallback callback) {
        RushCore.getInstance().save(this, callback);
    }

    @Override
    public void delete() {
        RushCore.getInstance().delete(this);
    }

    @Override
    public void delete(RushCallback rushCallback) {
        RushCore.getInstance().delete(this,rushCallback);
    }

    @Override
    public String getId() {
        return RushCore.getInstance().getId(this);
    }

    public String getBagId() {
        return bagId;
    }

    public void setBagId(String bagId) {
        this.bagId = bagId;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getDestinationName() {
        return destinationName;
    }

    public void setDestinationName(String destinationName) {
        this.destinationName = destinationName;
    }

    public String getDestinationId() {
        return destinationId;
    }

    public void setDestinationId(String destinationId) {
        this.destinationId = destinationId;
    }

    public String getCurrentNodeName() {
        return currentNodeName;
    }

    public void setCurrentNodeName(String currentNodeName) {
        this.currentNodeName = currentNodeName;
    }

    public Boolean getCanBePicked() {
        return canBePicked;
    }

    public void setCanBePicked(Boolean canBePicked) {
        this.canBePicked = canBePicked;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public boolean isScanned() {
        return isScanned;
    }

    public void setScanned(boolean scanned) {
        isScanned = scanned;
    }

    public String getReturnReason() {
        return returnReason;
    }

    public void setReturnReason(String returnReason) {
        this.returnReason = returnReason;
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

}
