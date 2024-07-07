package com.goflash.dispatch.features.cash.presenter

import android.content.Context
import com.goflash.dispatch.features.cash.view.CreateSummaryView

interface CreateSummaryPresenter {

    fun onAttachView(context: Context,view: CreateSummaryView)

    fun onDetachView()

    fun getBalances()

    fun clearData()

    fun createSummary()
}