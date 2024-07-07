package com.goflash.dispatch.features.lastmile.tripCreation.presenter

import android.content.Context
import com.goflash.dispatch.data.TripDTO
import com.goflash.dispatch.features.lastmile.tripCreation.view.MergeCreatedTripView
import com.goflash.dispatch.features.lastmile.tripCreation.view.MergeTripRowView

interface MergeCreatedTripPresenter {

    fun onAttachView(context: Context, view: MergeCreatedTripView)

    fun onDetachView()

    fun getTrips(excludedTripId: Long)

    fun getCount(): Int

    fun bindViewHolder(position: Int, holder: MergeTripRowView)

    fun onTripSelected(position: Int)

    fun mergeTrips()

    fun filterSprinters(str: String)

    fun clearFilter()

}