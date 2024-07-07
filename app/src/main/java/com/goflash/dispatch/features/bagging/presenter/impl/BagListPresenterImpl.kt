package com.goflash.dispatch.features.bagging.presenter.impl

import android.content.Context
import androidx.core.content.ContextCompat
import com.goflash.dispatch.R
import com.goflash.dispatch.api_services.SessionService
import com.goflash.dispatch.data.BagDTO
import com.goflash.dispatch.di.interactor.SortationApiInteractor
import com.goflash.dispatch.features.bagging.presenter.BagListPresenter
import com.goflash.dispatch.features.bagging.view.BagListView
import com.goflash.dispatch.features.bagging.view.BagRowView
import com.goflash.dispatch.type.BagStatus
import com.goflash.dispatch.util.PreferenceHelper
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription

class BagListPresenterImpl(private val sortationApiInteractor: SortationApiInteractor) : BagListPresenter {

    private val TAG: String = BagListPresenterImpl::class.java.name

    private var context : Context? = null

    private var bagListView: BagListView? = null
    private var compositeSubscription: CompositeSubscription? = null

    private var bagList = mutableListOf<BagDTO>()

    private val destinationList = mutableListOf<String>()
    private var filtered_bag_list = mutableListOf<BagDTO>()

    private var destination: String = ""
    private var bagId: String = ""

    private var filter_applied = false

    override fun onAttachView(context: Context, view: BagListView) {
        this.context = context
        this.bagListView = view
        compositeSubscription = CompositeSubscription()
    }

    override fun onDetachView() {
        if (bagListView == null)
            return
        bagListView = null
        compositeSubscription?.unsubscribe()
        compositeSubscription = null
    }

    override fun getBagList() {
        compositeSubscription?.add(
                sortationApiInteractor.getBagList(PreferenceHelper.assignedAssetId.toString())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            bagList.addAll(it)
                            bagListView?.setCount(bagList.size)
                            bagListView?.setSpinner(getDestinationList())
                            bagListView?.refreshList()
                        }, { error ->
                            bagListView?.onFailure(error)

                        })
        )
    }

    override fun OnBindBagRowView(position: Int, holder: BagRowView) {
        val bag = if (filter_applied)
            filtered_bag_list.get(position)
        else
            bagList.get(position)

        holder.setBagId(bag.bagId)
        holder.setDestination(bag.destinationName ?: "")
        holder.setBagStatus(bag.bagStatus)
        holder.setOnCLickListeners()

        holder.setTextColor(when (bag.bagStatus) {
            BagStatus.CREATED.name -> ContextCompat.getColor(context!!,R.color.text_color_blue)
            BagStatus.DISPATCHED.name -> ContextCompat.getColor(context!!,R.color.md_orange_800)
            BagStatus.RECEIVED.name -> ContextCompat.getColor(context!!,R.color.status_green)
            else -> ContextCompat.getColor(context!!,R.color.button_red)
        })
    }

    override fun getCount(): Int {
        return if (filter_applied)
            filtered_bag_list.size
        else
            bagList.size
    }


    private fun getDestinationList(): MutableList<String> {
        val result =
                bagList
                        .groupBy { it.destinationName ?: "" }

        destinationList.addAll(result.keys)
        return destinationList
    }

    override fun onDestinationSelected(position: Int) {
        destination = destinationList.get(position)
        getFilteredList()

    }

    private fun getFilteredList() {
        filtered_bag_list.clear()

        filtered_bag_list.addAll(
                when {
                    destination.isNotEmpty() && bagId.isNotEmpty() -> bagList.filter {
                        it.bagId.toLowerCase().contains(bagId.toLowerCase()) && it.destinationName.equals(
                                destination
                        )
                    }
                    bagId.isNotEmpty() -> bagList.filter { it.bagId.toLowerCase().contains(bagId.toLowerCase()) }
                    else -> bagList.filter { it.destinationName.equals(destination) }
                })



        filter_applied = true
        bagListView?.refreshList()
        bagListView?.setCount(filtered_bag_list.size)

    }

    override fun clearFilter(dest: Boolean) {
        filter_applied = false
        if (dest)
            destination = ""
        else
            bagId = ""
        bagListView?.refreshList()
        bagListView?.setCount(bagList.size)
    }

    override fun getBagById(str: String) {
        bagId = str
        getFilteredList()

    }

    override fun onBagItemClicked(position: Int) {
        val bag: BagDTO = when (filter_applied) {
            true -> {
                filtered_bag_list[position]
            }
            false -> {
                bagList[position]
            }
        }
        if (bag.bagStatus.equals(BagStatus.CREATED.name))
            bagListView?.startDiscardBagActivity(bag.bagId)
    }

}