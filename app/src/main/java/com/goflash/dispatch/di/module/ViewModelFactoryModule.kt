package com.goflash.dispatch.di.module

import androidx.lifecycle.ViewModelProvider
import com.goflash.dispatch.util.ViewModelProviderFactory
import dagger.Binds
import dagger.Module

@Module
interface ViewModelFactoryModule {

    @Binds
    fun bindViewModelFactory(factory: ViewModelProviderFactory): ViewModelProvider.Factory
}