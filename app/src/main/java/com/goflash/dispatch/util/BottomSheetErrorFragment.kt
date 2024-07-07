package com.goflash.dispatch.util

import android.os.Bundle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.goflash.dispatch.R
import com.goflash.dispatch.app_constants.LABEL

class BottomSheetErrorFragment : BottomSheetDialogFragment(), View.OnClickListener {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.bottom_sheet_cancel_order_fragment, container, false)
        (view.findViewById(R.id.tv_label) as TextView).text = arguments?.getString(LABEL)

        (view.findViewById(R.id.tvDismiss) as TextView).setOnClickListener(this)

        return view
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tvDismiss -> dismiss()
        }
    }
}