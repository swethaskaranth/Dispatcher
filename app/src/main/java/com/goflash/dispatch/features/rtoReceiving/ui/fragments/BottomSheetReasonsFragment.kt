package com.goflash.dispatch.features.rtoReceiving.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.goflash.dispatch.R
import com.goflash.dispatch.databinding.LayoutRasieIssueRtoBinding
import com.goflash.dispatch.features.rtoReceiving.listeners.RaiseIssueListener
import com.goflash.dispatch.features.rtoReceiving.listeners.ReasonSelectionListener
import com.goflash.dispatch.features.rtoReceiving.ui.adapter.RtoReasonAdapter
import com.goflash.dispatch.ui.fragments.BaseBottomSheetFragment
import com.goflash.dispatch.ui.interfaces.FragmentToActivityCommunicator

class BottomSheetReasonsFragment : BaseBottomSheetFragment(), ReasonSelectionListener {

    private lateinit var listener: RaiseIssueListener
    private lateinit var wayBillNumber: String
    private lateinit var status: String

    private lateinit var exceptions: List<String>

    lateinit var binding: LayoutRasieIssueRtoBinding

    private var exceptionsSelected: MutableList<String> = mutableListOf()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = activity as RaiseIssueListener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val args = arguments
        wayBillNumber = args?.getString("wayBillNumber") ?: ""
        status = args?.getString("status") ?: ""
        exceptions = args?.getStringArrayList("exceptions") ?: ArrayList()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LayoutRasieIssueRtoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvHeading.text =
            String.format(getString(R.string.raise_issue_for_awb), wayBillNumber)
        binding.tvRtoCancelled.text = status

        binding.rvReasons.layoutManager = LinearLayoutManager(requireActivity())
        binding.rvReasons.adapter = RtoReasonAdapter(requireActivity(), exceptions, this)

        binding.tvReject.isEnabled = false
        binding.tvReject.setBackgroundResource(R.drawable.reject_background_inactive)

        binding.tvAccept.setOnClickListener {
            listener.onStatusSelected("ACCEPT", wayBillNumber, exceptionsSelected)
            dismiss()
        }

        binding.tvReject.setOnClickListener {
            listener.onStatusSelected("REJECT", wayBillNumber, exceptionsSelected)
            dismiss()
        }

    }

    override fun onReasonSelected(position: Int) {
        if (exceptionsSelected.isEmpty()) {
            binding.tvReject.isEnabled = true
            binding.tvReject.setBackgroundResource(R.drawable.reject_background_active)
        }
        exceptionsSelected.add(exceptions[position])

    }

    override fun onReasonUnselected(position: Int) {
        exceptionsSelected.remove(exceptions[position])
        if (exceptionsSelected.isEmpty()) {
            binding.tvReject.isEnabled = false
            binding.tvReject.setBackgroundResource(R.drawable.reject_background_inactive)
        }
    }
}