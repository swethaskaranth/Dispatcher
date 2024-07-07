package com.goflash.dispatch.features.dispatch.presenter.impl

import android.content.Context
import co.uk.rushorm.core.RushSearch
import com.goflash.dispatch.data.BagDTO
import com.goflash.dispatch.features.dispatch.presenter.SizeSelectionPresenter
import com.goflash.dispatch.features.dispatch.view.SizeSelectionView
import rx.subscriptions.CompositeSubscription

class SizeSelectionPresenterImpl(): SizeSelectionPresenter {

    private var mView: SizeSelectionView? = null

    private var compositeSubscription: CompositeSubscription? = null

    override fun onAttachView(context: Context, view: SizeSelectionView) {
        this.mView = view
        compositeSubscription = CompositeSubscription()
        getBagData()
    }

    override fun onDetachView() {
        if (mView == null)
            return
        mView = null
        compositeSubscription?.unsubscribe()
        compositeSubscription = null
    }

    private fun getBagData(){
        val bags = RushSearch().find(BagDTO::class.java)
        mView?.onBagsFetched(bags)
        mView?.enableProceed(!bags.any { it.weight == null || it.weight == 0.0 })
    }

    override fun onSizeSelected(bag: BagDTO, size: Double) {
        val bagDTO = RushSearch().whereEqual("bagId", bag.bagId).findSingle(BagDTO::class.java)
        bagDTO.weight = size
        bagDTO.save()

        checkToEnableProceed()
    }

    private fun checkToEnableProceed(){
        val bags = RushSearch().find(BagDTO::class.java)
        mView?.enableProceed(!bags.any { it.weight == null || it.weight == 0.0 })
    }
}