package com.goflash.dispatch.di.components

import com.goflash.dispatch.di.module.FragmentModule
import com.goflash.dispatch.di.FragmentScope
import com.goflash.dispatch.features.cash.ui.fragment.TripCollectionFragment
import com.goflash.dispatch.features.lastmile.tripCreation.ui.activity.SelectSprinterForZoneFragment
import com.goflash.dispatch.features.lastmile.tripCreation.ui.fragments.*
import dagger.Component

/**
 * Created by Binay on 12/02/19.
 */
@FragmentScope
@Component(dependencies = [NetworkComponent::class], modules = [FragmentModule::class])
interface FragmemntComponent {
    /**\
     * @param loginActivity [ injecting fragments, will be used same for all injection just changes the fragment ]
     * */

    fun inject(createdFragment: CreatedFragment)

    fun inject(ofdFragment: OfdFragment)

    fun inject(completedFragment: CompletedFragment)

    fun inject(reconFinishFragment: ReconFinishFragment)

    fun inject(bottomSheetCancelFragment: BottomSheetCancelFragment)

    fun inject(bottomSheetUnblockFragment: BottomSheetUnblockFragment)

    fun inject(selectSprinterForZoneActivity: SelectSprinterForZoneFragment)

    fun inject(tripCollectionFragment: TripCollectionFragment)

}