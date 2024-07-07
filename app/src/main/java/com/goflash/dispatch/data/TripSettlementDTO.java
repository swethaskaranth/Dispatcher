package com.goflash.dispatch.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import co.uk.rushorm.core.Rush;
import co.uk.rushorm.core.RushCallback;
import co.uk.rushorm.core.RushCore;
import co.uk.rushorm.core.annotations.RushList;

/**
 * Created by Ravi on 2020-07-14.
 */
public class TripSettlementDTO implements Parcelable, Rush {

    @RushList(classType = UndeliveredShipmentDTO.class)
    private List<UndeliveredShipmentDTO> undeliveredShipment = new ArrayList<>();

    @RushList(classType = ReturnShipmentDTO.class)
    private List<ReturnShipmentDTO> returnShipment = new ArrayList<>();

    @RushList(classType = LostDamagedShipment.class)
    private List<LostDamagedShipment> lostDamagedShipments = new ArrayList<>();

    @RushList(classType = FmPickedShipment.class)
    private Map<String, List<FmPickedShipment>> fmPickedupShipments;

    private ReceiveCashDTO receiveCash;

    private ReceiveChequeDTO receiveCheque;

    private ReceiveNetBankDTO receiveNetBank;

    private ReceiveCdsCash cdsCashCollection;

    @RushList(classType = AckForRecon.class)
    private Map<String, AckForRecon> ackSlips;

    private Long tripId;

    private Long cashAmountReceived;
    private Long chequeAmountReceived;

    private boolean undeliveredScanned;
    private boolean returnScanned;
    private boolean fmPickupScanned;

    @RushList(classType = PoaResponseForRecon.class)
    private List<PoaResponseForRecon> poas;

    private boolean poasCaptured;

    public TripSettlementDTO(){}

    public TripSettlementDTO(List<UndeliveredShipmentDTO> undeliveredShipment, List<ReturnShipmentDTO> returnShipment, List<LostDamagedShipment> lostDamagedShipments, Map<String, List<FmPickedShipment>> fmPickedupShipments, ReceiveCashDTO receiveCash, ReceiveChequeDTO receiveCheque, ReceiveNetBankDTO receiveNetBank, ReceiveCdsCash cdsCashCollection, Map<String, AckForRecon> ackSlips, Long tripId, Long cashAmountReceived, Long chequeAmountReceived, boolean undeliveredScanned, boolean returnScanned, boolean fmPickupScanned, List<PoaResponseForRecon> poas, boolean poasCaptured) {
        this.undeliveredShipment = undeliveredShipment;
        this.returnShipment = returnShipment;
        this.lostDamagedShipments = lostDamagedShipments;
        this.fmPickedupShipments = fmPickedupShipments;
        this.receiveCash = receiveCash;
        this.receiveCheque = receiveCheque;
        this.receiveNetBank = receiveNetBank;
        this.cdsCashCollection = cdsCashCollection;
        this.ackSlips = ackSlips;
        this.tripId = tripId;
        this.cashAmountReceived = cashAmountReceived;
        this.chequeAmountReceived = chequeAmountReceived;
        this.undeliveredScanned = undeliveredScanned;
        this.returnScanned = returnScanned;
        this.fmPickupScanned = fmPickupScanned;
        this.poas = poas;
        this.poasCaptured = poasCaptured;
    }

    protected TripSettlementDTO(Parcel in) {
        undeliveredShipment = in.createTypedArrayList(UndeliveredShipmentDTO.CREATOR);
        returnShipment = in.createTypedArrayList(ReturnShipmentDTO.CREATOR);
        receiveCash = in.readParcelable(ReceiveCashDTO.class.getClassLoader());
        receiveCheque = in.readParcelable(ReceiveChequeDTO.class.getClassLoader());
        receiveNetBank = in.readParcelable(ReceiveNetBankDTO.class.getClassLoader());
        if (in.readByte() == 0) {
            tripId = null;
        } else {
            tripId = in.readLong();
        }
        if (in.readByte() == 0) {
            cashAmountReceived = null;
        } else {
            cashAmountReceived = in.readLong();
        }
        if (in.readByte() == 0) {
            chequeAmountReceived = null;
        } else {
            chequeAmountReceived = in.readLong();
        }
        undeliveredScanned = in.readByte() != 0;
        returnScanned = in.readByte() != 0;
        cdsCashCollection = in.readParcelable(ReceiveCdsCash.class.getClassLoader());
        lostDamagedShipments = in.createTypedArrayList(LostDamagedShipment.CREATOR);
       // fmPickedupShipments = in.readParcelable(FmPickedupShipmentsDTO.class.getClassLoader());
        fmPickupScanned = in.readByte() != 0;
        poas = in.createTypedArrayList(PoaResponseForRecon.CREATOR);
        poasCaptured = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(undeliveredShipment);
        dest.writeTypedList(returnShipment);
        dest.writeParcelable(receiveCash, flags);
        dest.writeParcelable(receiveCheque, flags);
        dest.writeParcelable(receiveNetBank, flags);
        if (tripId == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(tripId);
        }
        if (cashAmountReceived == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(cashAmountReceived);
        }
        if (chequeAmountReceived == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(chequeAmountReceived);
        }
        dest.writeByte((byte) (undeliveredScanned?1:0));
        dest.writeByte((byte) (returnScanned?1:0));
        dest.writeParcelable(cdsCashCollection,flags);
        dest.writeTypedList(lostDamagedShipments);
      //  dest.writeParcelable(fmPickedupShipments,flags);
        dest.writeByte((byte) (fmPickupScanned?1:0));
        dest.writeTypedList(poas);
        dest.writeByte((byte) (poasCaptured?1:0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<TripSettlementDTO> CREATOR = new Creator<TripSettlementDTO>() {
        @Override
        public TripSettlementDTO createFromParcel(Parcel in) {
            return new TripSettlementDTO(in);
        }

        @Override
        public TripSettlementDTO[] newArray(int size) {
            return new TripSettlementDTO[size];
        }
    };

    public List<UndeliveredShipmentDTO> getUndeliveredShipment() {
        return undeliveredShipment;
    }

    public void setUndeliveredShipment(List<UndeliveredShipmentDTO> undeliveredShipment) {
        this.undeliveredShipment = undeliveredShipment;
    }

    public List<ReturnShipmentDTO> getReturnShipment() {
        return returnShipment;
    }

    public void setReturnShipment(List<ReturnShipmentDTO> returnShipment) {
        this.returnShipment = returnShipment;
    }

    public ReceiveCashDTO getReceiveCash() {
        return receiveCash;
    }

    public void setReceiveCash(ReceiveCashDTO receiveCash) {
        this.receiveCash = receiveCash;
    }

    public ReceiveChequeDTO getReceiveCheque() {
        return receiveCheque;
    }

    public void setReceiveCheque(ReceiveChequeDTO receiveCheque) {
        this.receiveCheque = receiveCheque;
    }

    public Long getCashAmountReceived() {
        return cashAmountReceived;
    }

    public void setCashAmountReceived(Long cashAmountReceived) {
        this.cashAmountReceived = cashAmountReceived;
    }

    public Long getChequeAmountReceived() {
        return chequeAmountReceived;
    }

    public void setChequeAmountReceived(Long chequeAmountReceived) {
        this.chequeAmountReceived = chequeAmountReceived;
    }

    public Long getTripId() {
        return tripId;
    }

    public void setTripId(Long tripId) {
        this.tripId = tripId;
    }

    public boolean isUndeliveredScanned() {
        return undeliveredScanned;
    }

    public void setUndeliveredScanned(boolean undeliveredScanned) {
        this.undeliveredScanned = undeliveredScanned;
    }

    public boolean isReturnScanned() {
        return returnScanned;
    }

    public void setReturnScanned(boolean returnScanned) {
        this.returnScanned = returnScanned;
    }

    public ReceiveCdsCash getCdsCashCollection() {
        return cdsCashCollection;
    }

    public void setCdsCashCollection(ReceiveCdsCash cdsCashCollection) {
        this.cdsCashCollection = cdsCashCollection;
    }

    public List<LostDamagedShipment> getLostDamagedShipments() {
        return lostDamagedShipments;
    }

    public void setLostDamagedShipments(List<LostDamagedShipment> lostDamagedShipments) {
        this.lostDamagedShipments = lostDamagedShipments;
    }

    public Map<String, List<FmPickedShipment>> getFmPickedupShipments() {
        return fmPickedupShipments;
    }

    public void setFmPickedupShipments(Map<String, List<FmPickedShipment>> fmPickedupShipments) {
        this.fmPickedupShipments = fmPickedupShipments;
    }

    public Map<String, AckForRecon> getAckSlips() {
        return ackSlips;
    }

    public void setAckSlips(Map<String, AckForRecon> ackSlips) {
        this.ackSlips = ackSlips;
    }

    public boolean isFmPickupScanned() {
        return fmPickupScanned;
    }

    public void setFmPickupScanned(boolean fmPickupScanned) {
        this.fmPickupScanned = fmPickupScanned;
    }

    public ReceiveNetBankDTO getReceiveNetBank() {
        return receiveNetBank;
    }

    public void setReceiveNetBank(ReceiveNetBankDTO receiveNetBank) {
        this.receiveNetBank = receiveNetBank;
    }

    public List<PoaResponseForRecon> getPoas() {
        return poas;
    }

    public void setPoas(List<PoaResponseForRecon> poas) {
        this.poas = poas;
    }

    public static Creator<TripSettlementDTO> getCREATOR() {
        return CREATOR;
    }

    public boolean isPoasCaptured() {
        return poasCaptured;
    }

    public void setPoasCaptured(boolean poasCaptured) {
        this.poasCaptured = poasCaptured;
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
    public String toString() {
        return "TripSettlementDTO{" +
                "undeliveredShipment=" + undeliveredShipment +
                ", returnedItems=" + returnShipment +
                ", receiveCash=" + receiveCash +
                ", receiveCheque=" + receiveCheque +
                ", tripId=" + tripId +
                ", cashAmountReceived=" + cashAmountReceived +
                ", chequeAmountReceived=" + chequeAmountReceived +
                ", receiveNetBank=" + receiveNetBank +
                '}';
    }
}
