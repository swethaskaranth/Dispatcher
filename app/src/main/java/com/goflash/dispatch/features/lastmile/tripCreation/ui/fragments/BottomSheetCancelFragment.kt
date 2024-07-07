package com.goflash.dispatch.features.lastmile.tripCreation.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import com.goflash.dispatch.R
import com.goflash.dispatch.SortationApplication
import com.goflash.dispatch.databinding.LayoutBottomSheetCancelBinding
import com.goflash.dispatch.di.components.DaggerFragmemntComponent
import com.goflash.dispatch.features.lastmile.tripCreation.listeners.FragmentListener
import com.goflash.dispatch.features.lastmile.tripCreation.presenter.CancelPresenter
import com.goflash.dispatch.features.lastmile.tripCreation.ui.activity.ScanToSearchActivity
import com.goflash.dispatch.features.lastmile.tripCreation.ui.activity.UnassignedShipmentActivity
import com.goflash.dispatch.features.lastmile.tripCreation.view.CancelView
import com.goflash.dispatch.ui.fragments.BaseBottomSheetFragment
import com.goflash.dispatch.ui.interfaces.OnCancelListener
import javax.inject.Inject


class BottomSheetCancelFragment  : BaseBottomSheetFragment(), CancelView, View.OnClickListener {

    @Inject
    lateinit var mPresenter: CancelPresenter

    private var fragmentToActivity: FragmentListener? = null

    private var onCancelListener : OnCancelListener? = null

    private var selection: String? = null
    private var shipmentId: String? = null
    private var referenceId: String? = null
    private var position: Int? = 0

    private lateinit var binding: LayoutBottomSheetCancelBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initDagger()
        shipmentId = arguments?.getString("shipmentId")
        position = arguments?.getInt("position")
        referenceId = arguments?.getString("referenceId")
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        try {
            fragmentToActivity = if(context is ScanToSearchActivity) context else context as UnassignedShipmentActivity
            //(activity as MainActivity).activityToFragmentCommunicator = this
        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement FragmentToActivityCommunicator")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = LayoutBottomSheetCancelBinding.inflate(inflater)

        binding.tvYes.setOnClickListener(this)
        binding.tvDismiss.setOnClickListener(this)
        addRadioButtons(binding.rvCancelReason)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvText.text = "Reference Id #$referenceId"
    }

    private fun initDagger() {
        DaggerFragmemntComponent.builder().networkComponent((activity!!.application as SortationApplication).getNetworkComponent())
            .build().inject(this@BottomSheetCancelFragment)

        mPresenter.onAttachView(activity!!, this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tvYes -> {
                showProgress()
                mPresenter.cancelShipement(selection!!, shipmentId!!)
            }

            R.id.tvDismiss -> dismiss()
        }
    }

    private fun addRadioButtons(radioGp: RadioGroup) {

        radioGp.setOnCheckedChangeListener { group, checkedId ->

            val selected = group.findViewById<RadioButton>(checkedId)

            when (selected.text) {
                getString(R.string.damaged) -> {
                    selection = getString(R.string.damaged)
                }

                getString(R.string.out_of_service_area) -> {
                    selection = getString(R.string.out_of_service_area)
                }

                getString(R.string.wrong_pincode) -> {
                    selection = getString(R.string.wrong_pincode)
                }

                getString(R.string.lost) -> {
                    selection = getString(R.string.lost)
                }
            }
        }
    }

    override fun onSuccess() {
        hideProgress()
        fragmentToActivity?.deleteOrUnblockShipment(position!!)
        dismiss()

    }

    override fun onFailure(error: Throwable?) {
        dismiss()
        hideProgress()
        processError(error)
    }
}