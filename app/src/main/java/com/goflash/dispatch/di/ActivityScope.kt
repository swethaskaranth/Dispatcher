package com.goflash.dispatch.di

import javax.inject.Scope

/**
 * Created by Ravi on 28/05/19.
 * Scope can be configurable to SOURCE, CLASS and RUNTIME
 */
@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class ActivityScope