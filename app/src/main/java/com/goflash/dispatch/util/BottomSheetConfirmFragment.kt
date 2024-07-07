package com.goflash.dispatch.util

import android.content.Context
import android.os.Bundle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.goflash.dispatch.R
import com.goflash.dispatch.app_constants.LABEL
import com.goflash.dispatch.features.bagging.ui.activity.DiscardBagActivity
import com.goflash.dispatch.ui.interfaces.FragmentToActivityCommunicator

class BottomSheetConfirmFragment : BottomSheetDialogFragment(), View.OnClickListener {

    private var fragmentToActivityCommunicator : FragmentToActivityCommunicator? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentToActivityCommunicator = activity as FragmentToActivityCommunicator
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.bottom_sheet_confirm_fragment, container, false)
        (view.findViewById(R.id.tv_label) as TextView).text = arguments?.getString(LABEL)
        (view.findViewById(R.id.tvYes) as TextView).setOnClickListener(this)
        (view.findViewById(R.id.tvDismiss) as TextView).setOnClickListener(this)

        return view
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tvYes -> fragmentToActivityCommunicator?.onSuccess()
            R.id.tvDismiss -> dismiss()
        }
    }
}