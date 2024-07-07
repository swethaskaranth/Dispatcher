package com.goflash.dispatch.features.lastmile.settlement.presenter.impl

import android.content.Context
import android.content.Intent
import co.uk.rushorm.core.RushSearch
import com.goflash.dispatch.app_constants.*
import com.goflash.dispatch.data.Item
import com.goflash.dispatch.di.interactor.SortationApiInteractor
import com.goflash.dispatch.features.lastmile.settlement.presenter.ReviewItemPresenter
import com.goflash.dispatch.features.lastmile.settlement.view.ReviewItemView
import com.goflash.dispatch.type.ReconStatus
import rx.subscriptions.CompositeSubscription
import kotlin.properties.Delegates

class ReviewItemPresenterImpl(private val sortationApiInteractor: SortationApiInteractor) :
    ReviewItemPresenter {

    private var mView: ReviewItemView? = null

    private var compositeSubscription: CompositeSubscription? = null

    private var items: MutableList<Item> = mutableListOf()

    private var returnedQuantity : Int = 0
    private var partialDelivery: Boolean = false

    override fun onAttachView(context: Context, view: ReviewItemView) {
        this.mView = view
        compositeSubscription = CompositeSubscription()
    }

    override fun sendIntent(intent: Intent?) {
        val itemUcode = intent?.getStringExtra(ucode)
        val batch = intent?.getStringExtra(batch_number)
        val name = intent?.getStringExtra(display_name)
        val shipmentId = intent?.getStringExtra(shipment_id)
        val itemId = intent?.getIntExtra(item_id, 0)
        partialDelivery = intent?.getBooleanExtra(partial_delivery, false)?: false

        getItems(itemId ?: 0, itemUcode, batch, name, shipmentId!!)
    }

    private fun getItems(
        itemId: Int,
        ucode: String?,
        batch: String?,
        name: String?,
        shipmentId: String
    ) {
        if (ucode == null || batch == null) {
            val item = RushSearch().whereEqual("itemId", itemId).findSingle(Item::class.java)
            items.add(item)
        } else {
            items = RushSearch().whereEqual("shipmentId", shipmentId).and()
                .whereEqual("ucode", ucode).and()
                .whereEqual("displayName", name).and()
                .whereEqual("batchNumber", batch)
                .find(Item::class.java)
        }

        returnedQuantity = items.map { it.returnedQuantity }.reduce { acc, i -> acc.plus(i) }

        mView?.setItemDetails(name, batch, items[0].returnReason)

        mView?.onItemsFetched(items)
    }

    override fun setAcceptReject(position: Int, reconStatus: ReconStatus, reason: String,rejectRemarks: String) {
        val acceptQuantity = items.filter { it.reconStatus == ReconStatus.ACCEPT.name }.size
        if(partialDelivery && acceptQuantity == returnedQuantity){
            mView?.showError("Accepted quantity cannot be greater than picked up quantity")
            return
        }
        val item =
            RushSearch().whereEqual("itemId", items[position].itemId).findSingle(Item::class.java)

        if (item != null) {
            item.reconStatus = reconStatus.name
            item.reconStatusReason = reason
            item.reconRemark = rejectRemarks;
            when(reconStatus){
                ReconStatus.ACCEPT -> item.reconAcceptedQuantity = 1
                ReconStatus.REJECT -> item.reconRejectedQuantity = 1
            }
            item.save()
        }

        items[position].reconStatus = reconStatus.name
        items[position].reconStatusReason = reason

        mView?.onItemsFetched(items)
    }

    override fun onDetachView() {
        if (mView == null)
            return
        mView = null
        compositeSubscription?.unsubscribe()
        compositeSubscription = null
    }
}