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

public class TripSettlementCompleteDTO implements Parcelable, Rush {

    @RushList(classType = UndeliveredShipmentDTO.class)
    private List<UndeliveredShipmentDTO> undeliveredShipment = new ArrayList<>();

    @RushList(classType = ReturnShipmentDTO.class)
    private List<ReturnShipmentDTO> returnedItems = new ArrayList<>();

    @RushList(classType = FmPickedShipment.class)
    private Map<String, List<FmPickedShipment>> fmPickedupShipments;

    private ReceiveCashDTO receiveCash;

    private ReceiveChequeDTO receiveCheque;

    private ReceiveNetBankDTO receiveNetBank;

    private ReceiveCdsCash cdsCashCollection;

    @RushList(classType = AckSlipDto.class)
    private List<AckSlipDto> ackSlips;

    private Long tripId;

    private Long cashAmountReceived;
    private Long chequeAmountReceived;

    private Map<Long, List<PoaSatisfiedDTO>> poaSatisfied;

    public TripSettlementCompleteDTO(){}

    public TripSettlementCompleteDTO(List<UndeliveredShipmentDTO> undeliveredShipment, List<ReturnShipmentDTO> returnedItems, ReceiveCashDTO receiveCash, ReceiveChequeDTO receiveCheque, Long tripId, Long cashAmountReceived, Long chequeAmountReceived, ReceiveCdsCash receiveCdsCash, Map<String, List<FmPickedShipment>> fmPickedupShipments, ReceiveNetBankDTO receiveNetBank,  List<AckSlipDto> ackSlips, Map<Long, List<PoaSatisfiedDTO>> poaSatisfied) {
        this.undeliveredShipment = undeliveredShipment;
        this.returnedItems = returnedItems;
        this.receiveCash = receiveCash;
        this.receiveCheque = receiveCheque;
        this.tripId = tripId;
        this.cashAmountReceived = cashAmountReceived;
        this.chequeAmountReceived = chequeAmountReceived;
        this.cdsCashCollection = receiveCdsCash;
        this.fmPickedupShipments = fmPickedupShipments;
        this.receiveNetBank = receiveNetBank;
        this.ackSlips = ackSlips;
        this.poaSatisfied = poaSatisfied;
    }

    protected TripSettlementCompleteDTO(Parcel in, ReceiveNetBankDTO receiveNetBank) {
        undeliveredShipment = in.createTypedArrayList(UndeliveredShipmentDTO.CREATOR);
        returnedItems = in.createTypedArrayList(ReturnShipmentDTO.CREATOR);
        receiveCash = in.readParcelable(ReceiveCashDTO.class.getClassLoader());
        receiveCheque = in.readParcelable(ReceiveChequeDTO.class.getClassLoader());
        this.receiveNetBank = in.readParcelable(ReceiveNetBankDTO.class.getClassLoader());
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
        cdsCashCollection = in.readParcelable(ReceiveCdsCash.class.getClassLoader());
    }

    public TripSettlementCompleteDTO(TripSettlementDTO settlementDTO){
        if(settlementDTO != null) {
            this.undeliveredShipment = settlementDTO.getUndeliveredShipment();
            this.returnedItems = settlementDTO.getReturnShipment();
            this.receiveCash = settlementDTO.getReceiveCash();
            this.receiveCheque = settlementDTO.getReceiveCheque();
            this.tripId = settlementDTO.getTripId();
            this.cashAmountReceived = settlementDTO.getCashAmountReceived();
            this.chequeAmountReceived = settlementDTO.getChequeAmountReceived();
            this.cdsCashCollection = settlementDTO.getCdsCashCollection();
            this.receiveNetBank = settlementDTO.getReceiveNetBank();
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(undeliveredShipment);
        dest.writeTypedList(returnedItems);
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
        dest.writeParcelable(cdsCashCollection,flags);
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
        return returnedItems;
    }

    public void setReturnShipment(List<ReturnShipmentDTO> returnShipment) {
        this.returnedItems = returnShipment;
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

    public ReceiveCdsCash getCdsCashCollection() {
        return cdsCashCollection;
    }

    public void setCdsCashCollection(ReceiveCdsCash cdsCashCollection) {
        this.cdsCashCollection = cdsCashCollection;
    }

    public Map<String, List<FmPickedShipment>> getFmPickedupShipments() {
        return fmPickedupShipments;
    }

    public void setFmPickedupShipments(Map<String, List<FmPickedShipment>> fmPickedupShipments) {
        this.fmPickedupShipments = fmPickedupShipments;
    }

    public ReceiveNetBankDTO getReceiveNetBank() {
        return receiveNetBank;
    }

    public void setReceiveNetBank(ReceiveNetBankDTO receiveNetBank) {
        this.receiveNetBank = receiveNetBank;
    }

    public List<AckSlipDto> getAckSlips() {
        return ackSlips;
    }

    public void setAckSlips(List<AckSlipDto> ackSlips) {
        this.ackSlips = ackSlips;
    }

    public Map<Long, List<PoaSatisfiedDTO>> getPoaSatisfied() {
        return poaSatisfied;
    }

    public void setPoaSatisfied(Map<Long, List<PoaSatisfiedDTO>> poaSatisfied) {
        this.poaSatisfied = poaSatisfied;
    }

    public static Creator<TripSettlementDTO> getCREATOR() {
        return CREATOR;
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
                ", returnedItems=" + returnedItems +
                ", receiveCash=" + receiveCash +
                ", receiveCheque=" + receiveCheque +
                ", tripId=" + tripId +
                ", cashAmountReceived=" + cashAmountReceived +
                ", chequeAmountReceived=" + chequeAmountReceived +
                ", receiveNetBank=" + receiveNetBank +
                '}';
    }
}
