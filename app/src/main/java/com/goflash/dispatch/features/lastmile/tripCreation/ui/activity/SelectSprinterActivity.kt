package com.goflash.dispatch.features.lastmile.tripCreation.ui.activity

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.recyclerview.widget.LinearLayoutManager
import com.goflash.dispatch.R
import com.goflash.dispatch.SortationApplication
import com.goflash.dispatch.app_constants.sprinter_name
import com.goflash.dispatch.app_constants.trip_id
import com.goflash.dispatch.app_constants.trip_status
import com.goflash.dispatch.data.SprinterList
import com.goflash.dispatch.databinding.LayoutSelectSprinterBinding
import com.goflash.dispatch.di.components.DaggerActivityComponents
import com.goflash.dispatch.features.lastmile.tripCreation.listeners.OnItemSelctedListener
import com.goflash.dispatch.features.lastmile.tripCreation.presenter.SelectSprinterPresenter
import com.goflash.dispatch.features.lastmile.tripCreation.view.SelectSprinterView
import com.goflash.dispatch.ui.activity.BaseActivity
import com.goflash.dispatch.features.lastmile.tripCreation.ui.adapter.SprinterAdapter
import com.goflash.dispatch.ui.itemDecoration.MarginItemDecoration
import javax.inject.Inject

class SelectSprinterActivity : BaseActivity(), SelectSprinterView, View.OnClickListener,
    OnItemSelctedListener {

    @Inject
    lateinit var presenter: SelectSprinterPresenter

    private val sprinterList: MutableList<SprinterList> = mutableListOf()
    private val filterList: MutableList<SprinterList> = mutableListOf()

    private var selectedSprinter: SprinterList? = null

    private var tripId: Long? = null

    private var createdTrip = false

    private lateinit var binding: LayoutSelectSprinterBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LayoutSelectSprinterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tripId = intent.getLongExtra(trip_id, 0)
        createdTrip = intent.getStringExtra(trip_status) == "CREATED"

        initDagger()
        initViews()
    }

    private fun initDagger() {
        DaggerActivityComponents.builder()
            .networkComponent((application as SortationApplication).getNetworkComponent())
            .build().inject(this@SelectSprinterActivity)

        presenter.onAttachView(this, this)

        showProgress()
    }

    private fun initViews() {
        binding.toolbar.toolbarTitle.text = getString(R.string.select_sprinter)

        binding.btnSave.isEnabled = false
        binding.btnSave.setBackgroundResource(R.drawable.grey_button_background)

        binding.rvSprinter.layoutManager = LinearLayoutManager(this)
        binding.rvSprinter.adapter =
            SprinterAdapter(
                this,
                sprinterList,
                this
            )
        binding.rvSprinter.addItemDecoration(
            MarginItemDecoration(
                resources.getDimension(R.dimen.margin_10).toInt()
            )
        )

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
                    filterSprinters(s.toString())
                else
                    clearFilter()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })

        binding.btnSave.setOnClickListener(this)
        binding.toolbar.iVProfileHome.setOnClickListener(this)
    }

    override fun onSprintersFetched(list: List<SprinterList>) {
        hideProgress()
        sprinterList.addAll(list)
        binding.rvSprinter.adapter?.notifyDataSetChanged()

    }

    override fun onFailure(error: Throwable?) {
        hideProgress()
        processError(error)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.iVProfileHome -> finish()
            R.id.btnSave -> {
                if (selectedSprinter != null) {
                    showProgress()
                    if (createdTrip)
                        presenter.assignSprinter(tripId!!, selectedSprinter!!)
                    else
                        presenter.createTrip(selectedSprinter!!)
                } else
                    onFailure(Throwable("Please select a Sprinter"))
            }
        }
    }

    override fun onItemSelected(position: Int) {
        hideKeyboard()
        selectedSprinter = if (filterList.isNotEmpty())
            filterList[position]
        else
            sprinterList[position]
        binding.btnSave.isEnabled = true
        binding.btnSave.setBackgroundResource(R.drawable.blue_button_background)
    }

    override fun onCreateTripSuccess(tripId: Long, sprinterName: String) {
        hideProgress()

        val intent = Intent(this, ScanShipmentActivity::class.java)
        intent.putExtra(trip_id, tripId)
        intent.putExtra(sprinter_name, sprinterName)
        startActivity(intent)
        finish()
    }

    override fun onAssignSuccess() {
        hideProgress()
        finish()
    }

    private fun filterSprinters(str: String) {
        filterList.clear()
        disableSaveBtn()
        filterList.addAll(sprinterList.filter { it.name.contains(str, true) })
        binding.rvSprinter.adapter =
            SprinterAdapter(
                this,
                filterList,
                this
            )
    }

    private fun clearFilter() {
        filterList.clear()
        disableSaveBtn()
        binding.rvSprinter.adapter =
            SprinterAdapter(
                this,
                sprinterList,
                this
            )
    }

    private fun disableSaveBtn() {
        selectedSprinter = null
        binding.btnSave.isEnabled = false
        binding.btnSave.setBackgroundResource(R.drawable.grey_button_background)
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDetachView()
    }
}