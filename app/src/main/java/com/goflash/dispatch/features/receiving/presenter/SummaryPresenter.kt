package com.goflash.dispatch.features.receiving.presenter

import android.content.Context
import android.content.Intent
import com.goflash.dispatch.features.receiving.view.SummaryView

/**
 *Created by Ravi on 2019-09-08.
 */
interface SummaryPresenter {

    fun onAttachView(context: Context, view: SummaryView)

    fun onDetachView()

    fun onTaskResume()

    fun onIntent(intent: Intent)

    fun onCompleteTask()
}