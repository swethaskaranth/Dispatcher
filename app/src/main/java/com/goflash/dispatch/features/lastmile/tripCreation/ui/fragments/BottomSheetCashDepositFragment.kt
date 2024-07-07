package com.goflash.dispatch.features.lastmile.tripCreation.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.goflash.dispatch.R
import com.goflash.dispatch.data.CdsCashCollection
import com.goflash.dispatch.databinding.FragmentCashDepositBinding
import com.goflash.dispatch.features.lastmile.tripCreation.ui.adapter.CashDepositAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BottomSheetCashDepositFragment : BottomSheetDialogFragment(), View.OnClickListener {
    private lateinit var list : ArrayList<CdsCashCollection>

    private lateinit var binding: FragmentCashDepositBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        list = arguments?.getSerializable("transactions") as ArrayList<CdsCashCollection>
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentCashDepositBinding.inflate(layoutInflater)
        binding.tvDismiss.setOnClickListener(this)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvTransactions.layoutManager = LinearLayoutManager(activity)
        val adapter = CashDepositAdapter(activity!!,list)
        binding.rvTransactions.adapter = adapter
    }

    override fun onClick(v: View?) {
        when (v?.id) {

            R.id.tvDismiss -> dismiss()
        }
    }
}