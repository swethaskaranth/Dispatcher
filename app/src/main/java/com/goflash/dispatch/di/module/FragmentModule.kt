package com.goflash.dispatch.di.module

import com.goflash.dispatch.di.ActivityScope
import com.goflash.dispatch.di.FragmentScope
import com.goflash.dispatch.di.interactor.SortationApiInteractor
import com.goflash.dispatch.features.cash.presenter.CashCollectionBreakupPresenter
import com.goflash.dispatch.features.cash.presenter.impl.CashCollectionBreakupPresenterImpl
import com.goflash.dispatch.features.lastmile.tripCreation.presenter.*
import com.goflash.dispatch.features.lastmile.tripCreation.presenter.impl.*
import dagger.Module
import dagger.Provides

/**
 * Created by Binay on 12/02/19.
 */
@Module
class FragmentModule {

    /**
     * @param flashApiInteractor  [ API interactor will help to communicate over network by passing aprropriate param]
     * all provides will help to provide one instance of implementations of each presenter
     * Only variables is what presenter need to be provided and return based on that
     * */
    @Provides
    @FragmentScope
    fun provideCreatedPresenter(sortationApiInteractor: SortationApiInteractor): CreatedPresenter {
        return CreatedPresenterImpl(sortationApiInteractor)
    }

    @Provides
    @FragmentScope
    fun provideOfdPresenter(sortationApiInteractor: SortationApiInteractor): OfdPresenter {
        return OfdPresenterImpl(sortationApiInteractor)
    }

    @Provides
    @FragmentScope
    fun providCompletedPresenter(sortationApiInteractor: SortationApiInteractor): CompletedPresenter {
        return CompletedPresenterImpl(sortationApiInteractor)
    }

    @Provides
    @FragmentScope
    fun provideRedonFinishPresenter(sortationApiInteractor: SortationApiInteractor): ReconFinishPresenter {
        return ReconFinishPresenterImpl(sortationApiInteractor)
    }

    @Provides
    @FragmentScope
    fun provideCancelShipmentPresenter(sortationApiInteractor: SortationApiInteractor): CancelPresenter {
        return CancelPresenterImpl(sortationApiInteractor)
    }

    @Provides
    @FragmentScope
    fun provideUnblockShipmentPresenter(sortationApiInteractor: SortationApiInteractor): UnblockPresenter {
        return UnblockPresenterImpl(sortationApiInteractor)
    }

    @Provides
    @FragmentScope
    fun provideZoneSprinterPresenter(sortationApiInteractor: SortationApiInteractor): ZoneSprinterPresenter {
        return ZoneSprinterPresenterImpl(sortationApiInteractor)
    }

    @Provides
    @FragmentScope
    fun provideCashCollectionBreakupPresenter(sortationApiInteractor: SortationApiInteractor): CashCollectionBreakupPresenter {
        return CashCollectionBreakupPresenterImpl(sortationApiInteractor)
    }

}