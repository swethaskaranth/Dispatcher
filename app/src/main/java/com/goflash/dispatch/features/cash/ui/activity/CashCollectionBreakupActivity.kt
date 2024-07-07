package com.goflash.dispatch.features.cash.ui.activity

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.goflash.dispatch.R
import com.goflash.dispatch.app_constants.CASH_CLOSING_ID
import com.goflash.dispatch.app_constants.CASH_CLOSING_TOTAL
import com.goflash.dispatch.databinding.LayoutCashCollectionBreakupBinding
import com.goflash.dispatch.features.cash.ui.adapter.CashCollectionBreakupAdapter
import com.goflash.dispatch.features.cash.ui.fragment.TripCollectionFragment
import com.goflash.dispatch.ui.activity.BaseActivity
import com.google.android.material.tabs.TabLayoutMediator

class CashCollectionBreakupActivity: BaseActivity() {

    private var cashClosingId : String? = null

    lateinit var adapter: CashCollectionBreakupAdapter

    private var isLoading = false

    private lateinit var binding: LayoutCashCollectionBreakupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LayoutCashCollectionBreakupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        cashClosingId = intent.getStringExtra(CASH_CLOSING_ID)

        initViews()
    }



    private fun initViews(){
        binding.tvTotalAmount.text = intent.getStringExtra(CASH_CLOSING_TOTAL)
        binding.ivClose.setOnClickListener{
            finish()
        }

        val viewPagerAdapter = ViewPagerAdapter(supportFragmentManager, lifecycle, cashClosingId)
        binding.viewPager.adapter = viewPagerAdapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) {tab, position ->
            when(position){
                0 -> tab.text = "Trip Collection"
                1 -> tab.text = "Adhoc Cash Collection"
            }
        }.attach()

    }



    class ViewPagerAdapter(supportFragmentManager: FragmentManager, lifeCycle: Lifecycle, val cashClosingId: String?): FragmentStateAdapter(supportFragmentManager, lifeCycle){
        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
           val fragment = TripCollectionFragment()
            val adhoc = position == 1

            val args = Bundle()
            args.putString("cashClosingId", cashClosingId)
            args.putBoolean("adhoc", adhoc)
            fragment.arguments = args
            return fragment
        }

    }
}