package com.goflash.dispatch.features.audit.presenter.impl

import android.content.Context
import com.goflash.dispatch.di.interactor.SortationApiInteractor
import com.goflash.dispatch.model.AuditHistory
import com.goflash.dispatch.features.audit.presenter.AuditSummaryPresenter
import com.goflash.dispatch.features.audit.view.AuditSummaryView
import com.goflash.dispatch.presenter.views.SummaryRowView
import com.goflash.dispatch.type.AuditStatus
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription
import java.text.SimpleDateFormat
import java.util.*

class AuditSummaryPresenterImpl(private val sortationApiInteractor: SortationApiInteractor) :
    AuditSummaryPresenter {


    private val TAG = AuditSummaryPresenterImpl::class.java.simpleName

    private var homeView: AuditSummaryView? = null

    private var compositeSubscription: CompositeSubscription? = null

    private val historyList = mutableListOf<AuditHistory>()

    override fun onAttachView(context: Context, view: AuditSummaryView) {
        this.homeView = view
        compositeSubscription = CompositeSubscription()
    }

    override fun onDetachView() {
        if (this.homeView == null)
            return
        homeView = null
        compositeSubscription?.unsubscribe()
        compositeSubscription = null
    }

    override fun getSummaryList() {
        compositeSubscription?.add(
            sortationApiInteractor.getSummaryList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ list ->
                    historyList.clear()
                    historyList.addAll(list)
                    homeView?.refreshList()
                }, { error ->
                    homeView?.onFailure(error)

                })
        )
    }

    override fun getCount(): Int {
        return historyList.size
    }

    override fun onBindSummaryRowView(position: Int, holder: SummaryRowView) {
        val item = historyList[position]
        holder.setDate(getTimeFromISODate(item.createdOn))
        holder.setName(item.userName)

        holder.setOnClickListener()
    }

    override fun onItemClicked(position: Int) {
        if (historyList[position].status == AuditStatus.PROCESSING.name)
            homeView?.showAlert()
        else
            homeView?.takeToSummaryScreen(
                historyList[position].id,
                historyList[position].createdOn,
                historyList[position].updatedOn
            )
    }

    private fun getTimeFromISODate(dateStr: String): String {

        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        val cal = Calendar.getInstance()
        cal.time = format.parse(dateStr)


        val dateFormat = SimpleDateFormat("dd-MM-yyyy | HH:mm a")
        val dateforrow = dateFormat.format(cal.time)

        return dateforrow

    }

}