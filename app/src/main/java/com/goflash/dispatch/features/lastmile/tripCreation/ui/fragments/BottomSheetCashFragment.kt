package com.goflash.dispatch.features.lastmile.tripCreation.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import co.uk.rushorm.core.RushSearch
import com.goflash.dispatch.R
import com.goflash.dispatch.data.*
import com.goflash.dispatch.databinding.LayoutCashBreakupBinding
import com.goflash.dispatch.features.lastmile.tripCreation.ui.adapter.CashCollectedAdapter
import com.goflash.dispatch.type.TaskStatus
import com.goflash.dispatch.ui.fragments.BaseBottomSheetFragment

class BottomSheetCashFragment  : BaseBottomSheetFragment(), View.OnClickListener {

    private var tripId: String? = null
    private var sprinterName: String? = null
    private var summary: Boolean? = false
    private var cash: Boolean? = false

    lateinit var mCashData: ReceiveCashDTO
    lateinit var mChequeData: ReceiveChequeDTO
    lateinit var mNeftData: ReceiveNetBankDTO

    private var lots: List<CashBreakUp> = mutableListOf()

    private lateinit var binding: LayoutCashBreakupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tripId = arguments?.getString("tripId")
        sprinterName = arguments?.getString("sprinterName")
        summary = arguments?.getBoolean("summary", false)
        cash = arguments?.getBoolean("cash", false)

        if(!summary!!){
            mCashData = getCashCollection().receiveCash
            mChequeData = getCashCollection().receiveCheque
            mNeftData = getCashCollection().receiveNetBank
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = LayoutCashBreakupBinding.inflate(layoutInflater)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvDismiss.setOnClickListener(this)
        binding.rvCashBreakup.layoutManager = LinearLayoutManager(requireActivity())

        if(summary!!){
            val data = RushSearch().whereEqual("tripId", tripId).and()
                .whereEqual("status", TaskStatus.COMPLETED.name)
                .find(TaskListDTO::class.java)
            val grouped = data.groupBy { CashBreakUp(referenceId = it.referenceId, amount = it.shipmentValue.toInt(), name = it.name) }
            lots = grouped.keys.map {
                CashBreakUp(it.referenceId, it.amount, it.name)
            }

        }else {
            if(cash!!) {
                val cash =
                    RushSearch().whereChildOf(ReceiveCashDTO::class.java, "currentTripBreakUp", mCashData.id)
                        .find(CashChequeCollectedDetailsDTO::class.java)
                val grouped = cash.groupBy { CashBreakUp(referenceId = it.referenceId, amount = it.amount.toInt(), name = it.customerName) }
                lots = grouped.keys.map {
                    CashBreakUp(it.referenceId, it.amount, it.name)
                }
            }else{
                binding.tvCash.text = getString(R.string.cheque_breakup)
                val cheque =
                    RushSearch().whereChildOf(ReceiveChequeDTO::class.java, "currentTripBreakUp", mChequeData.id)
                        .find(CashChequeCollectedDetailsDTO::class.java)
                for(i in 0 until cheque.size) {
                    cheque[i].paymentType = "cheque"
                }
                val neft =
                    RushSearch().whereChildOf(ReceiveNetBankDTO::class.java, "currentTripBreakUp", mNeftData.id)
                        .find(CashChequeCollectedDetailsDTO::class.java)
                for(i in 0 until neft.size) {
                    neft[i].paymentType = "neft"
                }

                val list:MutableList<CashChequeCollectedDetailsDTO> = mutableListOf()
                list.addAll(cheque)
                list.addAll(neft)
                val grouped = list.groupBy { CashBreakUp(referenceId = it.referenceId, amount = it.amount.toInt(), name = it.customerName,
                    transactionId = it.transactionId, paymentType = it.paymentType) }
                lots = grouped.keys.map {
                    CashBreakUp(it.referenceId, it.amount, it.name, it.transactionId, it.paymentType)
                }
            }
        }

        binding.rvCashBreakup.adapter = CashCollectedAdapter(requireActivity(), lots)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tvDismiss -> dismiss()
        }
    }

    private fun getCashCollection(): TripSettlementDTO {
        return RushSearch().whereEqual("tripId", tripId!!.toLong()).findSingle(TripSettlementDTO::class.java)
    }
}