package com.goflash.dispatch.features.dispatch.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.goflash.dispatch.R
import com.goflash.dispatch.SortationApplication
import com.goflash.dispatch.app_constants.seal_required
import com.goflash.dispatch.data.BagDTO
import com.goflash.dispatch.databinding.LayoutSizeSelectionBinding
import com.goflash.dispatch.di.components.DaggerActivityComponents
import com.goflash.dispatch.features.dispatch.presenter.SizeSelectionPresenter
import com.goflash.dispatch.features.dispatch.ui.adapter.SizeSelectionAdapter
import com.goflash.dispatch.features.dispatch.view.SizeSelectionView
import com.goflash.dispatch.ui.activity.BaseActivity
import javax.inject.Inject

class SizeSelectionActivity : BaseActivity(), SizeSelectionView, View.OnClickListener, SizeSelectionAdapter.onSpinnerItemSelected {

    @Inject
    lateinit var sizeSelectionPresenter: SizeSelectionPresenter

    private lateinit var binding: LayoutSizeSelectionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = LayoutSizeSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initDagger()
        initView()

    }

    private fun initDagger() {
        DaggerActivityComponents.builder()
            .networkComponent((application as SortationApplication).getNetworkComponent())
            .build().inject(this@SizeSelectionActivity)

        sizeSelectionPresenter.onAttachView(this, this)
    }

    private fun initView(){
        val toolbar = findViewById<View>(R.id.toolBar) as Toolbar
        setSupportActionBar(toolbar)

        val actionbar: ActionBar? = supportActionBar
        actionbar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_back_button_circular)
            setDisplayShowTitleEnabled(false)
        }

        binding.toolBar.toolbarTitle.text = getString(R.string.bag_details)

        binding.proceedBtn.text = getString(R.string.proceed)
        binding.proceedBtn.isEnabled = false
        binding.proceedBtn.setBackgroundColor(ContextCompat.getColor(this, R.color.border_blue))

        binding.proceedBtn.setOnClickListener(this)
    }

    override fun onBagsFetched(bags: MutableList<BagDTO>) {
        binding.rvBags.layoutManager = LinearLayoutManager(this)
        binding.rvBags.adapter = SizeSelectionAdapter(this, bags, this)

    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.proceed_btn -> {
                val intent = Intent(this, VehicleDetailActivity::class.java)
                intent.putExtra(seal_required,intent.getBooleanExtra(seal_required, true))
                startActivity(intent)
            }
        }
    }

    override fun onItemSelected(bag: BagDTO, size: Double) {
        sizeSelectionPresenter.onSizeSelected(bag, size)
    }

    override fun enableProceed(enable: Boolean) {
        binding.proceedBtn.isEnabled = enable
        if (enable)
            binding.proceedBtn.setBackgroundColor(ContextCompat.getColor(this, R.color.md_orange_800))
        else
            binding.proceedBtn.setBackgroundColor(ContextCompat.getColor(this, R.color.border_blue))
    }



}