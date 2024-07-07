package com.goflash.dispatch.features.dispatch.view

import com.goflash.dispatch.data.MidMileDispatchedRunsheet

interface RunsheetListView {

    fun onFailure(error: Throwable?)

    fun onRunsheetsFetched(list: List<MidMileDispatchedRunsheet>)
}