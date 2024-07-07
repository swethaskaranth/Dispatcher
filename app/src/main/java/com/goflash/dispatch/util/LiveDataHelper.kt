package com.goflash.dispatch.util

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.LiveData

object LiveDataHelper {
    val _percent = MediatorLiveData<Int>()

    fun updatePercentage(percent: Int){
        _percent.postValue(percent)
    }

    fun observePercentage(): LiveData<Int> {
        return _percent
    }

}