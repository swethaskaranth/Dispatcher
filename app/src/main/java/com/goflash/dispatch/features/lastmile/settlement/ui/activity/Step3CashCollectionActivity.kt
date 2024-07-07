package com.goflash.dispatch.features.lastmile.settlement.ui.activity

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import com.goflash.dispatch.R
import com.goflash.dispatch.SortationApplication
import com.goflash.dispatch.app_constants.FILTER_DEBOUNCE
import com.goflash.dispatch.data.*
import com.goflash.dispatch.databinding.ActivityStep3CashCollectionBinding
import com.goflash.dispatch.di.components.DaggerActivityComponents
import com.goflash.dispatch.features.lastmile.settlement.presenter.Step3Presenter
import com.goflash.dispatch.features.lastmile.settlement.view.Step3View
import com.goflash.dispatch.features.lastmile.tripCreation.ui.fragments.BottomSheetCashDepositFragment
import com.goflash.dispatch.features.lastmile.tripCreation.ui.fragments.BottomSheetCashFragment
import com.goflash.dispatch.ui.activity.BaseActivity
import com.goflash.dispatch.ui.activity.FinishActivity
import rx.subjects.PublishSubject
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class Step3CashCollectionActivity : BaseActivity(), Step3View, View.OnClickListener {

    @Inject
    lateinit var mPresenter: Step3Presenter

    private var tripId: String? = null
    private var sprinterName: String? = null
    private var cashStr: String? = "0"
    private var chequeStr: String? = "0"

    lateinit var mCashData: ReceiveCashDTO
    lateinit var mChequeData: ReceiveChequeDTO
    lateinit var mNeftData: ReceiveNetBankDTO
    private var mCDSData: ReceiveCdsCash? = null

    private lateinit var subject: PublishSubject<String>
    private lateinit var subject2: PublishSubject<String>

    private lateinit var binding: ActivityStep3CashCollectionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStep3CashCollectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initDagger()
        initViews()
    }

    private fun initViews() {

        tripId = intent.getStringExtra("tripId")
        sprinterName = intent.getStringExtra("sprinterName")

        mCashData = mPresenter.getCashCollection(tripId!!).receiveCash
        mChequeData = mPresenter.getCashCollection(tripId!!).receiveCheque
        mNeftData = mPresenter.getCashCollection(tripId!!).receiveNetBank
        mCDSData = mPresenter.getCashCollection(tripId!!).cdsCashCollection

        binding.layoutCashCollection.btnPaymentLayout.btnPayment.setOnClickListener(this)
        binding.toolBar.ivBack.setOnClickListener(this)
        binding.layoutCashCollection.txtCurrentAmount.setOnClickListener(this)
        binding.layoutCashCollection.txtChequeCollect.setOnClickListener(this)
        binding.layoutCashCollection.tvCashDeposit.setOnClickListener(this)

        binding.layoutCashCollection.btnPaymentLayout.btnPayment.isEnabled = false
        binding.layoutCashCollection.btnPaymentLayout.btnPayment.setBackgroundResource(R.drawable.disable_button)

        binding.layoutCashCollection.btnPaymentLayout.btnPayment.text = resources.getString(R.string.settle_trip)
        binding.toolBar.toolbarTitle.text = getString(R.string.cash_collection)
        binding.toolBar.tvSprinter.text = "Trip ID # $tripId - $sprinterName"

        if (mChequeData.count == 0 && mNeftData.count == 0) {
            binding.layoutCashCollection.tvChequeCollection.visibility = View.INVISIBLE
            binding.layoutCashCollection.chequeCollection.visibility = View.INVISIBLE
        }


        binding.layoutCashCollection.tvCashAmount.text =
            String.format(getString(R.string.cash_in_hand_text), mCashData.total.toInt())
        binding.layoutCashCollection.tvPendingAmount.text =
            String.format(getString(R.string.cash_in_hand_text), mCashData.pendingBalance.toInt())
        binding.layoutCashCollection.tvCurrentAmount.text = String.format(
            getString(R.string.cash_in_hand_text),
            mCashData.currentTripCashCollected.toInt()
        )

        binding.layoutCashCollection.tvChequeAmount.text = String.format(
            getString(R.string.cash_in_hand_text),
            mChequeData.currentTripChequeCollected.toInt() + mNeftData.currentTripNetBankCollected.toInt()
        )

        //CDS Data

        binding.layoutCashCollection.tvCashDeposit.text =
            String.format(getString(R.string.cash_in_hand_text), (mCDSData?.total ?: 0.0).toInt())

        subject = PublishSubject.create()
        subject2 = PublishSubject.create()
        subject.debounce(FILTER_DEBOUNCE, TimeUnit.MILLISECONDS)
            .subscribe {
                runOnUiThread {
                    filter(it, true)
                }
            }

        subject2.debounce(FILTER_DEBOUNCE, TimeUnit.MILLISECONDS)
            .subscribe {
                runOnUiThread {
                    filter(it, false)
                }
            }

        binding.layoutCashCollection.edtChequeCollected.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s!!.isNotEmpty())
                    subject2.onNext(s.toString().trim().toLowerCase())
                else
                    subject2.onNext("0".trim().toLowerCase())
            }
        })

        binding.layoutCashCollection.edtCashCollected.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s!!.isNotEmpty())
                    subject.onNext(s.toString().trim().toLowerCase())
                else
                    subject.onNext("0".trim().toLowerCase())
            }
        })

    }

    private fun initDagger() {
        DaggerActivityComponents.builder()
            .networkComponent((application as SortationApplication).getNetworkComponent()).build()
            .inject(this@Step3CashCollectionActivity)

        mPresenter.onAttach(this, this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_payment -> {
                showProgress()
                mPresenter.settleTrip(tripId!!, cashStr!!, chequeStr!!)
            }

            R.id.txtCurrentAmount -> showCashBreakup(true)
            R.id.txtChequeCollect -> showCashBreakup(false)
            R.id.ivBack -> finish()
            R.id.tvCashDeposit -> {
                if (mPresenter.getCashCollection(tripId!!).cdsCashCollection != null && mPresenter.getCashCollection(
                        tripId!!
                    ).cdsCashCollection.total > 0
                )
                    showTransactions(ArrayList(mPresenter.getCashCollection(tripId!!).cdsCashCollection.currentTripBreakUp))
            }

        }
    }

    private fun filter(amount: String, cash: Boolean) {
        if (cash) {
            binding.layoutCashCollection.tvTotalAmount.text = String.format(
                getString(R.string.cash_in_hand_text),
                (mCashData.total - amount.toLong()).toInt()
            )

            cashStr = binding.layoutCashCollection.edtCashCollected.text.toString()
            validSettleTrip((mCashData.total - amount.toLong()) < 0)
        } else {
            binding.layoutCashCollection.tvPendingChequeAmount.text = String.format(
                getString(R.string.cash_in_hand_text),
                ((mChequeData.currentTripChequeCollected + mNeftData.currentTripNetBankCollected) - amount.toDouble()).toInt()
            )
            chequeStr = binding.layoutCashCollection.edtChequeCollected.text.toString()
            validSettleTrip(((mChequeData.currentTripChequeCollected + mNeftData.currentTripNetBankCollected) - amount.toLong()) < 0)
        }
    }

    private fun validSettleTrip(disable: Boolean) {

        binding.layoutCashCollection.btnPaymentLayout.btnPayment.isEnabled = false
        binding.layoutCashCollection.btnPaymentLayout.btnPayment.setBackgroundResource(R.drawable.disable_button)

        if ((mChequeData.count != 0 || mNeftData.count != 0) && chequeStr!!.length < 0) {
            return
        }

        if (mCashData.total != 0L && cashStr!!.length < 0) {
            return
        }

        if(disable)
            return

        binding.layoutCashCollection.btnPaymentLayout.btnPayment.isEnabled = true
        binding.layoutCashCollection.btnPaymentLayout.btnPayment.setBackgroundResource(R.drawable.blue_button_background)
    }


    override fun onSuccess(message: String) {
        hideProgress()

        val intent = Intent(this, FinishActivity::class.java)
        intent.putExtra("tripId", tripId)
        intent.putExtra("message", message)
        startActivity(intent)
        finishAffinity()
    }

    override fun onFailure(error: Throwable?) {
        hideProgress()
        processError(error)
    }

    private fun showCashBreakup(cash: Boolean) {
        val frag = BottomSheetCashFragment()
        val args = Bundle()
        args.putString("tripId", tripId)
        args.putString("sprinterName", sprinterName)
        args.putBoolean("cash", cash)
        frag.arguments = args
        frag.isCancelable = false
        frag.show(supportFragmentManager, frag.tag)
    }

    override fun onDestroy() {
        super.onDestroy()
        mPresenter.onDetach()
    }

    fun showTransactions(list: ArrayList<CdsCashCollection?>?) {
        val fragment = BottomSheetCashDepositFragment()
        val args = Bundle()
        args.putSerializable("transactions", list)
        fragment.arguments = args
        fragment.show(supportFragmentManager, fragment.tag)
    }
}
