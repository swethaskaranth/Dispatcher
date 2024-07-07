package com.goflash.dispatch.features.lastmile.tripCreation.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.goflash.dispatch.R
import com.goflash.dispatch.data.ZoneDetails
import com.goflash.dispatch.features.lastmile.tripCreation.listeners.DemergeListener
import com.goflash.dispatch.ui.interfaces.FragmentToActivityCommunicator
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BottomSheetDemergeZoneFragment  : BottomSheetDialogFragment(), View.OnClickListener {

    private var listener : DemergeListener? = null

    private var zone : Int? = null

    private  var zoneName : String? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = activity as DemergeListener
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.layout_bottom_sheet_demerge_zone, container, false)

        zone = arguments?.getInt("zone")
        zoneName = arguments?.getString("zoneName")

        (view.findViewById(R.id.tvZoneName) as TextView).text = String.format(activity?.getString(R.string.zone_names)!!,zoneName)

        (view.findViewById(R.id.tvConfirm) as TextView).setOnClickListener(this)
        (view.findViewById(R.id.tvCancel) as TextView).setOnClickListener(this)

        return view
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tvConfirm -> {
                listener?.onConfirm(zone!!)
                dismiss()
            }
            R.id.tvCancel -> dismiss()
        }
    }

}