package com.goflash.dispatch.features.audit.presenter

import android.content.Context
import com.goflash.dispatch.features.audit.view.AuditSummaryView
import com.goflash.dispatch.presenter.views.SummaryRowView

interface AuditSummaryPresenter{

    fun onAttachView(context: Context, view: AuditSummaryView)

    fun onDetachView()

    fun getSummaryList()

    fun getCount() : Int

    fun onBindSummaryRowView(position : Int, holder: SummaryRowView)

    fun onItemClicked(position : Int)



}