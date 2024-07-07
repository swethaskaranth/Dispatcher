package com.goflash.dispatch.features.dispatch.presenter

import android.content.Context
import android.content.Intent
import com.goflash.dispatch.data.BagDTO
import com.goflash.dispatch.features.dispatch.view.SizeSelectionView

interface SizeSelectionPresenter {

    fun onAttachView(context: Context, view : SizeSelectionView)

    fun onDetachView()

    fun onSizeSelected(bag: BagDTO, size: Double)
}