package com.goflash.dispatch.features.lastmile.tripCreation.ui.activity

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.goflash.dispatch.R
import com.goflash.dispatch.SortationApplication
import com.goflash.dispatch.data.SprinterForZone
import com.goflash.dispatch.databinding.LayoutSelectSprinterForZoneBinding
import com.goflash.dispatch.di.components.DaggerFragmemntComponent
import com.goflash.dispatch.features.lastmile.tripCreation.listeners.SprinterFragmentListener
import com.goflash.dispatch.features.lastmile.tripCreation.listeners.ZoneSprinterListener
import com.goflash.dispatch.features.lastmile.tripCreation.presenter.ZoneSprinterPresenter
import com.goflash.dispatch.features.lastmile.tripCreation.ui.adapter.ZoneSprinterAdapter
import com.goflash.dispatch.features.lastmile.tripCreation.view.ZoneSprinterView
import com.goflash.dispatch.ui.fragments.BaseFragment
import com.goflash.dispatch.ui.itemDecoration.MarginItemDecoration
import javax.inject.Inject

class SelectSprinterForZoneFragment : BaseFragment(), ZoneSprinterView, View.OnClickListener,
    ZoneSprinterListener {

    @Inject
    lateinit var mPresenter: ZoneSprinterPresenter

    private var fragmentToActivity: SprinterFragmentListener? = null

    private val sprinterList: MutableList<SprinterForZone> = mutableListOf()
    private val filterList: MutableList<SprinterForZone> = mutableListOf()

    private val selectedList: MutableList<SprinterForZone> = mutableListOf()

    private var zone : Int? = null
    private var minSprinterCount: Int = -1

    private lateinit var binding: LayoutSelectSprinterForZoneBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        zone = arguments?.getInt("zone")
        minSprinterCount = arguments?.getInt("minSprinterCount") ?: -1
        selectedList.addAll(arguments?.getParcelableArrayList("sprinters")!!)

        initDagger()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            fragmentToActivity = context as AssignZonesActivity
        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement RefreshPage")
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = LayoutSelectSprinterForZoneBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()

        showProgress()
        mPresenter.getSprinters(selectedList)
    }

    private fun initDagger() {
        DaggerFragmemntComponent.builder()
            .networkComponent((activity!!.application as SortationApplication).getNetworkComponent())
            .build().inject(this)

        mPresenter.onAttachView(activity!!, this)

    }

    private fun initViews() {

        binding.toolbar.toolbarTitle.text = getString(R.string.select_sprinters)
        binding.rvSprinter.layoutManager = LinearLayoutManager(activity!!)
        binding.rvSprinter.adapter = ZoneSprinterAdapter(activity!!, sprinterList, this, selectedList)
        binding.rvSprinter.addItemDecoration(
            MarginItemDecoration(
                resources.getDimension(R.dimen.margin_10).toInt()
            )
        )

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

        binding.toolbar.iVProfileHome.setOnClickListener(this)
        binding.btnSave.btnClear.setOnClickListener(this)
        binding.btnSave.btnSave.setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.iVProfileHome -> {
                fragmentToActivity?.removeFragment()
            }
            R.id.btn_clear -> {
                selectedList.clear()
                binding.rvSprinter.adapter = ZoneSprinterAdapter(
                    activity!!,
                    if (filterList.isNotEmpty()) filterList else sprinterList,
                    this,
                    selectedList
                )
            }
            R.id.btn_save -> {
                // Validate input
                if(validateSprinterCount()) {
                    saveAndSendSprinters()
                }
            }
        }
    }

    override fun onSprintersFetched(list: List<SprinterForZone>) {
        hideProgress()
        sprinterList.addAll(list)
        binding.rvSprinter.adapter?.notifyDataSetChanged()
    }

    private fun filterSprinters(str: String) {
        filterList.clear()

        filterList.addAll(sprinterList.filter { it.name.contains(str, true) })
        binding.rvSprinter.adapter = ZoneSprinterAdapter(activity!!, filterList, this, selectedList)
    }

    private fun clearFilter() {
        filterList.clear()
        binding.rvSprinter.adapter = ZoneSprinterAdapter(activity!!, sprinterList, this, selectedList)

    }

    override fun addSprinterToList(sprinter: SprinterForZone) {
        if (!selectedList.contains(sprinter))
            selectedList.add(sprinter)
    }

    override fun removeSprinterFromList(sprinter: SprinterForZone) {
        selectedList.remove(sprinter)
    }

    override fun onFailure(error: Throwable?) {
        hideProgress()
        processError(error)
    }

    private fun saveAndSendSprinters(){
      //  RushCore.getInstance().deleteAll(SprinterForZone::class.java){
            for (sprinter in selectedList) {
                sprinter.isDisabled = true
                sprinter.save()
            }
     //   }


        fragmentToActivity?.setSprinterData(zone!!,selectedList)
    }

    override fun onDestroy() {
        super.onDestroy()
        mPresenter.onDetachView()
    }

    private fun validateSprinterCount(): Boolean{
        if(selectedList.size < minSprinterCount){
            error("Please select atleast $minSprinterCount sprinters")
            return false
        }
        return true
    }

}