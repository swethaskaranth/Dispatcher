package com.goflash.dispatch.features.cash.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.goflash.dispatch.R
import com.goflash.dispatch.SortationApplication
import com.goflash.dispatch.data.AdhocCashCollectionBreakup
import com.goflash.dispatch.data.CashCollectionTripDetails
import com.goflash.dispatch.databinding.LayoutCashCollectionBreakupFragmentBinding
import com.goflash.dispatch.di.components.DaggerFragmemntComponent
import com.goflash.dispatch.features.cash.presenter.CashCollectionBreakupPresenter
import com.goflash.dispatch.features.cash.ui.adapter.AdhocCashCollectionBreakupAdapter
import com.goflash.dispatch.features.cash.ui.adapter.CashCollectionBreakupAdapter
import com.goflash.dispatch.features.cash.ui.listener.PageScrollListener
import com.goflash.dispatch.features.cash.view.CashCollectionBreakupView
import com.goflash.dispatch.ui.fragments.BaseFragment
import com.goflash.dispatch.util.SimpleDividerItemDecoration
import javax.inject.Inject

class TripCollectionFragment: BaseFragment(), CashCollectionBreakupView {

    @Inject
    lateinit var mPresenter: CashCollectionBreakupPresenter

    private var cashClosingId : String? = null

    lateinit var adapter: CashCollectionBreakupAdapter
    lateinit var adhocAdapter: AdhocCashCollectionBreakupAdapter

    private var isLoading = false
    private var adhoc: Boolean = false

    private lateinit var binding: LayoutCashCollectionBreakupFragmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cashClosingId = arguments?.getString("cashClosingId")
        adhoc = arguments?.getBoolean("adhoc", false)?: false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LayoutCashCollectionBreakupFragmentBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initDagger()
        initViews()
    }

    fun initViews(){
        val layoutManager =  LinearLayoutManager(requireActivity())
        binding.rvBreakup.layoutManager = layoutManager
        adapter = CashCollectionBreakupAdapter(requireActivity(), mutableListOf())
        adhocAdapter = AdhocCashCollectionBreakupAdapter(requireActivity(), mutableListOf())
        binding.rvBreakup.adapter = if(adhoc) adhocAdapter else adapter
        binding.rvBreakup.addItemDecoration(
            SimpleDividerItemDecoration(
                activity
            )
        )

        binding.rvBreakup.addOnScrollListener(object : PageScrollListener(layoutManager){
            override fun loadMoreItems() {
                isLoading = true
                mPresenter.getCashCollectionBreakup(cashClosingId,20, adhoc)
            }

            override fun isLastPage(): Boolean {
                return mPresenter.isLastPage()
            }

            override fun isLoading(): Boolean {
                return isLoading
            }

        })

        mPresenter.getCashCollectionBreakup(cashClosingId,20, adhoc)
    }

    private fun initDagger() {
        DaggerFragmemntComponent.builder()
            .networkComponent((requireActivity().application as SortationApplication).getNetworkComponent())
            .build().inject(this)

        mPresenter.onAttach(requireActivity(), this)
    }

    override fun onFailure(error: Throwable?) {
        processError(error)
    }

    override fun onBreakupFetched(list: MutableList<CashCollectionTripDetails>) {
        isLoading = false
        adapter.addAll(list)
    }

    override fun onAdhocBreakupFetched(list: MutableList<AdhocCashCollectionBreakup>) {
        isLoading = false
        adhocAdapter.addAll(list)
    }

    override fun showNoElementsView() {
        binding.clHeader.visibility = View.GONE
        binding.tvNoElements.visibility = View.VISIBLE
    }
}