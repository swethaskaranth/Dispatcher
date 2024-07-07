package com.goflash.dispatch.features.cash.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.goflash.dispatch.R
import com.goflash.dispatch.SortationApplication
import com.goflash.dispatch.app_constants.CASH_CLOSING_TOTAL
import com.goflash.dispatch.data.BalancesDTO
import com.goflash.dispatch.databinding.LayoutCreateCashClosingSummaryBinding
import com.goflash.dispatch.di.components.DaggerActivityComponents
import com.goflash.dispatch.features.cash.presenter.CreateSummaryPresenter
import com.goflash.dispatch.features.cash.ui.fragment.BottomSheetConfirmFragment
import com.goflash.dispatch.features.cash.view.CreateSummaryView
import com.goflash.dispatch.ui.activity.BaseActivity
import com.goflash.dispatch.ui.interfaces.FragmentToActivityCommunicator
import com.goflash.dispatch.util.getDecimalFormat
import javax.inject.Inject

class CreateSummaryActivity : BaseActivity(), CreateSummaryView, View.OnClickListener,
    FragmentToActivityCommunicator {

    @Inject
    lateinit var mPresenter: CreateSummaryPresenter

    private lateinit var binding: LayoutCreateCashClosingSummaryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LayoutCreateCashClosingSummaryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initDagger()
        initViews()
    }

    private fun initViews() {

        binding.toolbar.toolbarTitle.text = getString(R.string.create_summary)

        binding.btnCreate.btnPayment.text = getString(R.string.create)

        binding.tvAddExpense.setOnClickListener(this)
        binding.tvAddCashPickup.setOnClickListener(this)
        binding.toolbar.iVProfileHome.setOnClickListener(this)
        binding.btnCreate.btnPayment.setOnClickListener(this)
        binding.txtTotalCollected.setOnClickListener(this)

        mPresenter.clearData()
    }


    private fun initDagger() {
        DaggerActivityComponents.builder()
            .networkComponent((application as SortationApplication).getNetworkComponent())
            .build().inject(this)

        mPresenter.onAttachView(this, this)

    }


    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.tvAddExpense -> startAddExpenseActivity()
            R.id.tvAddCashPickup -> startAddCashPickupActivity()
            R.id.iVProfileHome -> finish()
            R.id.btn_payment -> showBottomSheet()
            R.id.txtTotalCollected -> startCashBreakupActivity()
        }
    }

    private fun showBottomSheet() {
        binding.btnCreate.btnPayment.isEnabled = false
        val bottomsheet = BottomSheetConfirmFragment()
        bottomsheet.show(supportFragmentManager, bottomsheet.tag)
    }

    override fun onResume() {
        super.onResume()
        mPresenter.getBalances()
    }

    override fun onBalanceFetched(balancesDTO: BalancesDTO) {
        hideProgress()

        binding.tvCashAmount.text = String.format(
            getString(R.string.cash_in_hand_text_formatted),
            getDecimalFormat(balancesDTO.openingBalance ?: 0L)
        )
        binding.tvTotalCash.text = String.format(
            getString(R.string.cash_in_hand_text_formatted),
            getDecimalFormat(balancesDTO.totalCashCollected ?: 0L)
        )
        binding.tvTotalAmt.text = String.format(
            getString(R.string.cash_in_hand_text_formatted),
            getDecimalFormat(balancesDTO.totalAmount ?: 0L)
        )
        binding.tvCashInHand.text = String.format(
            getString(R.string.cash_in_hand_text_formatted),
            getDecimalFormat(balancesDTO.totalAmount ?: 0L)
        )
        binding.tvCashClosingBalance.text = String.format(
            getString(R.string.cash_in_hand_text_formatted),
            getDecimalFormat(balancesDTO.totalAmount ?: 0L)
        )

    }

    override fun setExpense(
        expenseCount: Int,
        amount: Long,
        totalAmount: Long,
        cashPickup: Long,
        cashClosing: Long
    ) {
        binding.tvCount.text = "$expenseCount"
        binding.tvExpenses.text =
            String.format(getString(R.string.cash_in_hand_text_formatted), getDecimalFormat(amount))
        binding.tvCashInHand.text = String.format(
            getString(R.string.cash_in_hand_text_formatted),
            getDecimalFormat(totalAmount)
        )
        binding.tvCashPickup.text = String.format(
            getString(R.string.cash_in_hand_text_formatted),
            getDecimalFormat(cashPickup)
        )
        binding.tvCashClosingBalance.text = String.format(
            getString(R.string.cash_in_hand_text_formatted),
            getDecimalFormat(cashClosing)
        )

        if (cashClosing < 0) {
            binding.btnCreate.btnPayment.isEnabled = false
            binding.btnCreate.btnPayment.setBackgroundResource(R.drawable.disable_button)
        } else {
            binding.btnCreate.btnPayment.isEnabled = true
            binding.btnCreate.btnPayment.setBackgroundResource(R.drawable.blue_button_background)
        }

    }

    override fun onFailure(error: Throwable?) {
        hideProgress()
        binding.btnCreate.btnPayment.isEnabled = true
        processError(error)
    }

    private fun startAddExpenseActivity() {
        startActivity(
            Intent(
                this,
                AddExpenseActivity::class.java
            )
        )
    }

    private fun startAddCashPickupActivity() {
        startActivity(
            Intent(
                this,
                AddCashPickupActivity::class.java
            )
        )
    }

    private fun startCashBreakupActivity() {
        val intent = Intent(
            this,
            CashCollectionBreakupActivity::class.java
        )
        intent.putExtra(CASH_CLOSING_TOTAL,binding.tvTotalCash.text.toString())
        startActivity(intent)
    }

    override fun onSuccess() {
        showProgress()
        mPresenter.createSummary()
    }

    override fun onError(message: String) {
        super.onError(message)
        binding.btnCreate.btnPayment.isEnabled = true
    }

    override fun onCreateSuccess() {
        hideProgress()
        val intent = Intent(this, CashClosingActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
    }

    override fun onDestroy() {
        mPresenter.onDetachView()
        super.onDestroy()

    }


}