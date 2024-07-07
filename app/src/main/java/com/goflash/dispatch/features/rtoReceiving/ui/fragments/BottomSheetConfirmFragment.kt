package com.goflash.dispatch.features.rtoReceiving.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.goflash.dispatch.databinding.LayoutConfirmReceivingCompleteBinding
import com.goflash.dispatch.features.rtoReceiving.listeners.ConfirmCompleteListener
import com.goflash.dispatch.ui.fragments.BaseBottomSheetFragment

class BottomSheetConfirmFragment: BaseBottomSheetFragment() {

    lateinit var listener: ConfirmCompleteListener

    private lateinit var binding: LayoutConfirmReceivingCompleteBinding

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.listener = activity as ConfirmCompleteListener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = LayoutConfirmReceivingCompleteBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnConfirm.setOnClickListener {
            listener.onComplete()
        }

        binding.btnCancel.setOnClickListener { dismiss() }
    }
}