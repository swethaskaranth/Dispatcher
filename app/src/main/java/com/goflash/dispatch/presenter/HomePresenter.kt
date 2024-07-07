package com.goflash.dispatch.presenter

import android.content.Context
import com.goflash.dispatch.presenter.views.HomeView

interface HomePresenter{

    fun onAttachView(context: Context, view: HomeView)

    fun onDetachView()

    fun checkIfDispatchStarted()

    fun getSummary()

    fun getInwardRuns()

}