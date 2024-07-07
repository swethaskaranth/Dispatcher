package com.goflash.dispatch.features.cash.presenter.impl

import android.content.Context
import co.uk.rushorm.core.RushCore
import co.uk.rushorm.core.RushSearch
import com.goflash.dispatch.api_services.SessionService
import com.goflash.dispatch.data.BalancesDTO
import com.goflash.dispatch.data.CashClosingRequest
import com.goflash.dispatch.data.CashPickupDTO
import com.goflash.dispatch.data.ExpenseDTO
import com.goflash.dispatch.di.interactor.SortationApiInteractor
import com.goflash.dispatch.features.cash.presenter.CreateSummaryPresenter
import com.goflash.dispatch.features.cash.view.CreateSummaryView
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription

class CreateSummaryPresenterImpl(private val sortationApiInteractor: SortationApiInteractor) :
    CreateSummaryPresenter {

    private var mView: CreateSummaryView? = null

    private var compositeSubscription: CompositeSubscription? = null

    private var balanceDto: BalancesDTO? = null

    private var expenses: MutableList<ExpenseDTO> = mutableListOf()

    private var cashPicked: CashPickupDTO? = null

    var expenseAmount = 0L
    var cashAmount = 0L

    override fun onAttachView(context: Context, view: CreateSummaryView) {
        this.mView = view
        compositeSubscription = CompositeSubscription()
    }

    override fun onDetachView() {
        if (mView == null)
            return
        mView = null
        compositeSubscription?.unsubscribe()
        compositeSubscription = null
    }

    override fun clearData() {
        RushSearch().find(ExpenseDTO::class.java) {
            RushCore.getInstance().delete(it)
        }

        RushSearch().find(CashPickupDTO::class.java) {
            RushCore.getInstance().delete(it)
        }
    }

    override fun getBalances() {
        compositeSubscription?.add(sortationApiInteractor.getBalances()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                balanceDto = it
                mView?.onBalanceFetched(balanceDto!!)
                getExpenseData(balanceDto!!.totalAmount ?: 0)

            }, {
                mView?.onFailure(it)
            })
        )
    }

    private fun getExpenseData(totalAmount: Long) {

        expenses = RushSearch().find(ExpenseDTO::class.java)
        expenseAmount = if (expenses.isEmpty()) 0 else expenses.map { it.amount }
            .reduce { acc, l -> acc.plus(l) }

        cashPicked = RushSearch().findSingle(CashPickupDTO::class.java)

        cashAmount = if (cashPicked != null) getCashPicked(cashPicked!!) else 0

        mView?.setExpense(
            expenses?.size ?: 0,
            expenseAmount ?: 0,
            totalAmount.minus(expenseAmount ?: 0),
            cashAmount,
            totalAmount.minus(expenseAmount ?: 0).minus(cashAmount)
        )
    }

    private fun getCashPicked(cashPickupDTO: CashPickupDTO): Long {
        var amount = 0L

        amount += (cashPickupDTO.twoThousand ?: 0) * 2000
        amount += (cashPickupDTO.fiveHundred ?: 0) * 500
        amount += (cashPickupDTO.twoHundred ?: 0) * 200
        amount += (cashPickupDTO.hundred ?: 0) * 100
        amount += (cashPickupDTO.fifty ?: 0) * 50
        amount += (cashPickupDTO.twenty ?: 0) * 20
        amount += (cashPickupDTO.ten ?: 0) * 10
        amount += (cashPickupDTO.five ?: 0) * 5
        amount += (cashPickupDTO.two ?: 0) * 2
        amount += (cashPickupDTO.one ?: 0) * 1

        return amount
    }

    override fun createSummary() {

        val request = getRequestBody()

        compositeSubscription?.add(sortationApiInteractor.createCashSummary(request)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                mView?.onCreateSuccess()

            }, {
                mView?.onFailure(it)
            })
        )


    }

    private fun getRequestBody(): CashClosingRequest {

        return CashClosingRequest(
            expenses,
            cashPicked?.cashDepositFileUrl,
            cashPicked?.cashDepositReceiptNumber ?: "",
            balanceDto?.openingBalance?:0L,
            balanceDto?.totalAmount?.minus(expenseAmount)!!.minus(cashAmount),
            cashAmount,
            balanceDto?.totalCashCollected?:0L,
            expenseAmount,
            cashPicked?.one?:0L,
            cashPicked?.two?:0L,
            cashPicked?.five?:0L,
            cashPicked?.ten?:0L,
            cashPicked?.twenty?:0L,
            cashPicked?.fifty?:0L,
            cashPicked?.hundred?:0L,
            cashPicked?.twoHundred?:0L,
            cashPicked?.fiveHundred?:0L,
            cashPicked?.twoThousand?:0L,
            balanceDto?.lastTripId!!,
            depositType = cashPicked?.depositType,
            atmId = cashPicked?.atmId
        )
    }


}