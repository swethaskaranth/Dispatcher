package com.goflash.dispatch.features.lastmile.tripCreation.ui.activity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.recyclerview.widget.LinearLayoutManager
import com.goflash.dispatch.R
import com.goflash.dispatch.SortationApplication
import com.goflash.dispatch.app_constants.trip_id
import com.goflash.dispatch.databinding.LayoutSelectSprinterBinding
import com.goflash.dispatch.di.components.DaggerActivityComponents
import com.goflash.dispatch.features.lastmile.tripCreation.presenter.MergeCreatedTripPresenter
import com.goflash.dispatch.features.lastmile.tripCreation.ui.adapter.MergeCreatedTripAdapter
import com.goflash.dispatch.features.lastmile.tripCreation.view.MergeCreatedTripView
import com.goflash.dispatch.ui.activity.BaseActivity
import javax.inject.Inject

class MergeCreatedTripActivity : BaseActivity(), MergeCreatedTripView, View.OnClickListener {

    @Inject
    lateinit var presenter: MergeCreatedTripPresenter

    private lateinit var binding: LayoutSelectSprinterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LayoutSelectSprinterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        initDagger()
    }

    private fun initViews() {
        binding.labelSelect.text = getString(R.string.select_a_trip_to_which_shipments_will_be_added)
        binding.toolbar.toolbarTitle.text = getString(R.string.merge_trip)
        binding.btnSave.text = getString(R.string.merge)
        binding.searchFilter.etSearch.setHint(R.string.search_by_tripid_or_sprinter_name)

        binding.rvSprinter.layoutManager = LinearLayoutManager(this)

        enableMerge(false)

        binding.btnSave.setOnClickListener(this)
        binding.toolbar.iVProfileHome.setOnClickListener(this)

        binding.searchFilter.etSearch.setOnEditorActionListener { v, actionId, event ->
            if(actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyboard()
                true
            }
            false
        }
        binding.searchFilter.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s?.isNotEmpty() == true)
                    presenter.filterSprinters(s.toString())
                else
                    presenter.clearFilter()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })
    }

    private fun initDagger() {
        DaggerActivityComponents.builder()
            .networkComponent((application as SortationApplication).getNetworkComponent())
            .build().inject(this@MergeCreatedTripActivity)

        presenter.onAttachView(this, this)

        binding.rvSprinter.adapter = MergeCreatedTripAdapter(this, presenter)

        showProgress()
        presenter.getTrips(intent.getLongExtra(trip_id, 0))

    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.iVProfileHome -> finish()
            R.id.btnSave -> {
                showProgress()
                presenter.mergeTrips()
            }
        }
    }

    override fun onFailure(error: Throwable?) {
        hideProgress()
        processError(error)
    }

    override fun refreshList() {
        hideProgress()

        binding.rvSprinter.adapter?.notifyDataSetChanged()

    }

    override fun enableMerge(enable: Boolean) {
        if(enable){
            binding.btnSave.isEnabled = true
            binding.btnSave.setBackgroundResource(R.drawable.blue_button_background)
        }else{
            binding.btnSave.isEnabled = false
            binding.btnSave.setBackgroundResource(R.drawable.grey_button_background)
        }
    }

    override fun onSuccess() {
        hideProgress()
        finish()
    }


}