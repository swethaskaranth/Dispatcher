package com.goflash.dispatch.di.components

import android.app.Application
import com.goflash.dispatch.SortationApplication
import com.goflash.dispatch.di.interactor.SortationApiInteractor
import com.goflash.dispatch.di.module.NetworkModule
import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import javax.inject.Singleton

/**
 * Created by Ravi on 28/05/19.
 * Networkcomponent for dependency injection which handle through interactor method
 */
@Singleton
@Component(
    modules = [
        NetworkModule::class,
    ],
)
interface NetworkComponent {
    fun sortationApiInteractor(): SortationApiInteractor
}