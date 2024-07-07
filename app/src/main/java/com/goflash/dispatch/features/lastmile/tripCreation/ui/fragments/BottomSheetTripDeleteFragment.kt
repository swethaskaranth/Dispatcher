package com.goflash.dispatch.features.lastmile.tripCreation.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.goflash.dispatch.R
import com.goflash.dispatch.ui.interfaces.FragmentToActivityCommunicator
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BottomSheetTripDeleteFragment : BottomSheetDialogFragment(), View.OnClickListener {

    private var listener : FragmentToActivityCommunicator? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = activity as FragmentToActivityCommunicator
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.layout_bottom_sheet_delete_trip, container, false)

        (view.findViewById(R.id.tvConfirm) as TextView).setOnClickListener(this)
        (view.findViewById(R.id.tvCancel) as TextView).setOnClickListener(this)

        return view
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tvConfirm -> {
                listener?.onSuccess()
                dismiss()
            }
            R.id.tvCancel -> dismiss()
        }
    }
}