package com.goflash.dispatch.features.bagging.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.ActionBar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.appcompat.widget.Toolbar
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.goflash.dispatch.R
import com.goflash.dispatch.SortationApplication
import com.goflash.dispatch.app_constants.bag_id
import com.goflash.dispatch.databinding.ActivityBagListBinding
import com.goflash.dispatch.di.components.DaggerActivityComponents
import com.goflash.dispatch.features.bagging.presenter.BagListPresenter
import com.goflash.dispatch.features.bagging.view.BagListView
import com.goflash.dispatch.ui.activity.BaseActivity
import com.goflash.dispatch.features.bagging.ui.adapter.BagAdapter
import com.goflash.dispatch.ui.itemDecoration.MarginItemDecoration
import javax.inject.Inject

class BagListActivity : BaseActivity(), BagListView {

    @Inject
    lateinit var bagListPresenter: BagListPresenter

    private var spinner_initialized = false

    lateinit var binding: ActivityBagListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBagListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initDagger()
        initViews()
    }

    fun initDagger() {
        DaggerActivityComponents.builder().networkComponent((application as SortationApplication).getNetworkComponent())
                .build().inject(this@BagListActivity)

        bagListPresenter.onAttachView(this, this@BagListActivity)
    }

    fun initViews() {
        val toolbar = findViewById<View>(R.id.toolBar1) as Toolbar
        setSupportActionBar(toolbar)

        binding.toolBar1.toolbarTitle.text = getString(R.string.bag_listing)

        val actionbar: ActionBar? = supportActionBar
        actionbar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_back_button_circular)
            setDisplayShowTitleEnabled(false)
        }

        showProgress()
        bagListPresenter.getBagList()

        binding.rvBag.layoutManager = LinearLayoutManager(this)
        binding.rvBag.adapter = BagAdapter(this, bagListPresenter)
        binding.rvBag.addItemDecoration(MarginItemDecoration(resources.getDimension(R.dimen.margin_10).toInt()))

        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(str: Editable?) {
                if(str?.length!! > 0)
                    bagListPresenter.getBagById(str.toString())
                else
                    bagListPresenter.clearFilter(false)

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

        }

        binding.searchBagIdEt.addTextChangedListener(textWatcher)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {

                finish()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bagListPresenter.onDetachView()
    }

    override fun onFailure(error: Throwable?) {
        hideProgress()
        processError(error)
    }

    override fun refreshList() {
        hideProgress()
        binding.rvBag.adapter?.notifyDataSetChanged()
    }

    override fun setCount(count: Int) {
        binding.tvBagLabel.text = String.format(getString(R.string.bags_label), count)
        if(count == 0)
            binding.tvNoBagsFound.visibility = View.VISIBLE
        else
            binding.tvNoBagsFound.visibility = View.GONE

    }

    override fun setSpinner(list: MutableList<String>) {

        list.add(0, getString(R.string.select_destination))
        val destination_adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list)

        binding.destinationSpinner.adapter = destination_adapter
        binding.destinationSpinner.setSelection(0) //display hint


        binding.destinationSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                if (!spinner_initialized) {
                    spinner_initialized = true
                } else
                    if (pos != 0) {
                        showProgress()
                        bagListPresenter.onDestinationSelected(pos)
                    }else
                        bagListPresenter.clearFilter(true)

            }

        }

    }

    override fun startDiscardBagActivity(bagId: String) {
        val intent = Intent(this, DiscardBagActivity::class.java)
        intent.putExtra(bag_id,bagId)
        startActivity(intent)
    }

}