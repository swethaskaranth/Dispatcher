package com.goflash.dispatch.features.rtoReceiving.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.goflash.dispatch.databinding.LayoutEnterBarcodeManualBinding
import com.goflash.dispatch.features.rtoReceiving.listeners.EnterBarcodeListener
import com.goflash.dispatch.ui.fragments.BaseBottomSheetFragment

class BottomSheetEnterBarcodeFragment : BaseBottomSheetFragment() {

    lateinit var binding: LayoutEnterBarcodeManualBinding

    lateinit var listener: EnterBarcodeListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = activity as EnterBarcodeListener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LayoutEnterBarcodeManualBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSubmit.setOnClickListener {
            listener.onBarcodeEntered(binding.etBarcode.text.toString())
            dismiss()
        }
    }
}