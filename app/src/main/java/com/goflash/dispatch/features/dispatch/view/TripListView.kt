package com.goflash.dispatch.features.dispatch.view

interface TripListView {

    fun onFailure(error: Throwable?)

    fun refreshList()
}