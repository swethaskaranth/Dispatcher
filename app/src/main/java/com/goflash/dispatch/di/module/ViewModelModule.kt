package com.goflash.dispatch.di.module

import androidx.lifecycle.ViewModel
import com.goflash.dispatch.features.lastmile.settlement.AcknowledgeSlipViewModel
import dagger.Binds
import dagger.MapKey
import dagger.Module
import dagger.multibindings.IntoMap
import kotlin.reflect.KClass


@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER
)
@MapKey
annotation class ViewModelKey(val value: KClass<out ViewModel>)

@Module
abstract class ViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(AcknowledgeSlipViewModel::class)
    abstract fun bindAcknowledgeSlipViewModel(viewModel: AcknowledgeSlipViewModel) : ViewModel
}