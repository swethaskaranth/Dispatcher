package com.goflash.dispatch.features.lastmile.settlement

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import co.uk.rushorm.core.RushSearch
import com.goflash.dispatch.data.Poa
import com.goflash.dispatch.data.PoaResponseForRecon
import com.goflash.dispatch.data.TripSettlementDTO
import com.goflash.dispatch.data.UndeliveredShipmentDTO
import com.goflash.dispatch.type.PoaType

class AcknowledgeSlipViewModel : ViewModel() {

    private val _ackSlips = MutableLiveData<List<PoaResponseForRecon>>()

    val ackSlips: LiveData<List<PoaResponseForRecon>> =
        _ackSlips


    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String>
        get() = _errorMessage

    private val _allOwNext = MutableLiveData<Boolean>()
    val allowNext: LiveData<Boolean>
        get() = _allOwNext

    fun initialize(tripId: Long) {

        val trip =
            RushSearch().whereEqual("tripId", tripId).findSingle(TripSettlementDTO::class.java)

        val poas = RushSearch().whereChildOf(
            TripSettlementDTO::class.java,
            "poas",
            trip.id
        )
            .find(PoaResponseForRecon::class.java)

        poas?.let { list ->
            _ackSlips.postValue(list.filter { poaDetails ->
                poaDetails.poaResponse.mandatoryPoa.any { it.type == PoaType.VERIFY_CASH_RECEIPT.name }
                        || poaDetails.poaResponse.optionalPoa.any { it.type == PoaType.VERIFY_CASH_RECEIPT.name }
            }.map { poaData ->
                poaData.isMandatory =
                    poaData.poaResponse.mandatoryPoa.any { it.type == PoaType.VERIFY_CASH_RECEIPT.name }
                poaData.cashReceiptBarcode = poaData.referenceId
                var cashPoa = poaData.poaResponse.mandatoryPoa.find{poa -> poa.type == PoaType.VERIFY_CASH_RECEIPT.name}
                if(cashPoa == null)
                    cashPoa = poaData.poaResponse.optionalPoa.find{poa -> poa.type == PoaType.VERIFY_CASH_RECEIPT.name}
                if(cashPoa?.regex?.isNotEmpty() == true){
                    val reg = cashPoa.regex.replace("\\\\","\\", false)
                    val regex = Regex(reg)
                    val matchedArray = regex.find(poaData.referenceId)
                    if(matchedArray != null && matchedArray.groupValues.isNotEmpty())
                        poaData.cashReceiptBarcode = matchedArray.groupValues[1]

                }
                poaData
            })

            _allOwNext.postValue(!list.any { it.isMandatory && !it.isScanned })

        }
    }

    fun onBarcodeScanned(barcode: String) {
        val ackSlips = _ackSlips.value
        val matchedAckSlip = ackSlips?.find { it.cashReceiptBarcode == barcode }
        matchedAckSlip?.let { ackSlip ->
            if (ackSlip.isScanned)
                _errorMessage.postValue("Acknowledgement already scanned")
            else {
                matchedAckSlip.isScanned = true
                matchedAckSlip.save()
                _ackSlips.postValue(ackSlips!!)
                _allOwNext.postValue(!ackSlips.any { it.isMandatory && !it.isScanned })
            }
        } ?: _errorMessage.postValue("Invalid barcode")

    }
}