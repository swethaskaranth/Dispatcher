package com.goflash.dispatch.features.lastmile.tripCreation.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.goflash.dispatch.R
import com.goflash.dispatch.SortationApplication
import com.goflash.dispatch.databinding.LayoutBottomSheetUnblockBinding
import com.goflash.dispatch.di.components.DaggerFragmemntComponent
import com.goflash.dispatch.features.lastmile.tripCreation.listeners.FragmentListener
import com.goflash.dispatch.features.lastmile.tripCreation.presenter.UnblockPresenter
import com.goflash.dispatch.features.lastmile.tripCreation.ui.activity.ScanToSearchActivity
import com.goflash.dispatch.features.lastmile.tripCreation.ui.activity.UnassignedShipmentActivity
import com.goflash.dispatch.features.lastmile.tripCreation.view.UnblockView
import com.goflash.dispatch.ui.fragments.BaseBottomSheetFragment
import javax.inject.Inject


class BottomSheetUnblockFragment  : BaseBottomSheetFragment(),UnblockView, View.OnClickListener {

    @Inject
    lateinit var mPresenter: UnblockPresenter

    private var fragmentToActivity: FragmentListener? = null

    private var referenceId: String? = null
    private var shipmentId: String? = null
    private var postponed: String? = null
    private var position: Int? = 0

    private lateinit var binding: LayoutBottomSheetUnblockBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initDagger()

        referenceId = arguments?.getString("referenceId")
        shipmentId = arguments?.getString("shipmentId")
        postponed = arguments?.getString("postponed")
        position = arguments?.getInt("position")
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            fragmentToActivity = if(context is ScanToSearchActivity) context else context as UnassignedShipmentActivity
            //(activity as MainActivity).activityToFragmentCommunicator = this
        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement FragmentListener")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = LayoutBottomSheetUnblockBinding.inflate(inflater)

        binding.tvYes.setOnClickListener(this)
        binding.tvDismiss.setOnClickListener(this)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvOrderId.text = String.format(getString(R.string.set_bold), referenceId, postponed)
    }

    private fun initDagger() {
        DaggerFragmemntComponent.builder().networkComponent((activity!!.application as SortationApplication).getNetworkComponent())
            .build().inject(this@BottomSheetUnblockFragment)

        mPresenter.onAttachView(activity!!, this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tvYes -> {
                showProgress()
                mPresenter.unBlockShipement(shipmentId!!, referenceId!!)
            }
            R.id.tvDismiss -> dismiss()
        }
    }

    override fun onSuccess() {
        hideProgress()
        fragmentToActivity?.deleteOrUnblockShipment(position!!)
        dismiss()
    }

    override fun onFailure(error: Throwable?) {
        hideProgress()
        processError(error)
        dismiss()
    }
}