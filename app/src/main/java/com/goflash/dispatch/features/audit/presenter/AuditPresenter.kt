package com.goflash.dispatch.features.audit.presenter

import android.content.Context
import com.goflash.dispatch.features.audit.view.AuditView

interface AuditPresenter{

    fun onAttachView(context: Context, view: AuditView)

    fun onDetachView()

    fun beginAudit()

}