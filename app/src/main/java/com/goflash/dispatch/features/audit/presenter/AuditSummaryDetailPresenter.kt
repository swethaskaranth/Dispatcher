package com.goflash.dispatch.features.audit.presenter

import android.content.Context
import android.content.Intent
import com.goflash.dispatch.features.audit.view.AuditSummaryDetailView

interface AuditSummaryDetailPresenter {

    fun onAttachView(context: Context, view : AuditSummaryDetailView)

    fun onDetachView()

    fun sendIntent(intent: Intent)
}