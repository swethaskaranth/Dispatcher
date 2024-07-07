package com.goflash.dispatch.features.dispatch.view

import com.goflash.dispatch.data.BagDTO

interface SizeSelectionView {

    fun onBagsFetched(bags: MutableList<BagDTO>)

    fun enableProceed(enable: Boolean)
}