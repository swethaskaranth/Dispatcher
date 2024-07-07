package com.goflash.dispatch.data;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

import co.uk.rushorm.core.Rush;
import co.uk.rushorm.core.RushCallback;
import co.uk.rushorm.core.RushCore;

public class ReceiveShipmentConfig implements Rush, Serializable {

    boolean sortationRequired;
    boolean runsheetRequired;
    boolean rejectFlowRequired;

    public boolean isSortationRequired() {
        return sortationRequired;
    }

    public void setSortationRequired(boolean sortationRequired) {
        this.sortationRequired = sortationRequired;
    }

    public boolean isRunsheetRequired() {
        return runsheetRequired;
    }

    public void setRunsheetRequired(boolean runsheetRequired) {
        this.runsheetRequired = runsheetRequired;
    }

    public boolean isRejectFlowRequired() {
        return rejectFlowRequired;
    }

    public void setRejectFlowRequired(boolean rejectFlowRequired) {
        this.rejectFlowRequired = rejectFlowRequired;
    }

    public void save() {
        RushCore.getInstance().saveOnlyWithoutConflict((Rush) this);
    }

    public void save(@NotNull RushCallback callback) {
        RushCore.getInstance().save((Rush) this, callback);
    }

    public void delete() {
        RushCore.getInstance().delete((Rush) this);
    }

    public void delete(@NotNull RushCallback callback) {
        RushCore.getInstance().delete((Rush) this, callback);
    }

    @NotNull
    public String getId() {
        return RushCore.getInstance().getId(this);
    }
}
