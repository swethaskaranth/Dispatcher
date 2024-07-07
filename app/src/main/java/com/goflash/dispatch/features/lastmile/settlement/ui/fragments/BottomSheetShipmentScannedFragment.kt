package com.goflash.dispatch.features.lastmile.settlement.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.goflash.dispatch.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BottomSheetShipmentScannedFragment: BottomSheetDialogFragment(), View.OnClickListener {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.bottom_sheet_shipment_scanned, container, false)
        (view.findViewById(R.id.tvScannedText) as TextView).text = String.format(requireActivity().getString(R.string.is_already_scanned),arguments?.getString("LABEL"))

        (view.findViewById(R.id.tvDismiss) as TextView).setOnClickListener(this)

        return view
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tvDismiss -> dismiss()
        }
    }

}