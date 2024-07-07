package com.goflash.dispatch.features.cash.ui.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.goflash.dispatch.R
import com.goflash.dispatch.features.cash.listeners.FileTypeSelectListener
import com.goflash.dispatch.ui.fragments.BaseBottomSheetFragment

class BottomSheetChooseFileFragment : BaseBottomSheetFragment() , View.OnClickListener{

    private var listener: FileTypeSelectListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = activity as FileTypeSelectListener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_choose_file_type, container, false)
        (view.findViewById(R.id.close) as ImageView).setOnClickListener(this)

        (view.findViewById(R.id.take_photo) as TextView).setOnClickListener(this)

        (view.findViewById(R.id.pdf) as TextView).setOnClickListener(this)

        return view
    }


    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.close -> dismiss()
            R.id.take_photo -> {

                listener?.onFileTypeSelected("IMAGE")
                dismiss()
            }
            R.id.pdf -> {
                listener?.onFileTypeSelected("PDF")
                dismiss()
            }
        }
    }

}