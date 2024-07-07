package com.goflash.dispatch.features.audit.presenter

import android.content.Context
import android.content.Intent
import com.goflash.dispatch.features.audit.view.AuditScanView

interface AuditScanPresenter{

    fun onAttachView(context: Context, view: AuditScanView)

    fun onDetachView()

    fun sendIntent(intent : Intent)

    fun onBarcodeScanned(barcode : String)

    fun completeAudit()


}