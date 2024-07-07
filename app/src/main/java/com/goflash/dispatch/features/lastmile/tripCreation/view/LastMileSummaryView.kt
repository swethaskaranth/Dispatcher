package com.goflash.dispatch.features.lastmile.tripCreation.view

import com.goflash.dispatch.data.TaskListDTO

interface LastMileSummaryView {

    fun onTasksFetched(completeTasks : LinkedHashMap<String,List<TaskListDTO>>, inCompleteTasks : LinkedHashMap<String,List<TaskListDTO>>)

    fun onFailure(error: Throwable?)

    fun onCashFetched(cash_value : Int, cds_cash: Int)

    fun hideProgressBar()

}