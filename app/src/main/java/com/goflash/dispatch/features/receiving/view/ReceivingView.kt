package com.goflash.dispatch.features.receiving.view

import com.goflash.dispatch.data.ReceivingDto

/**
 *Created by Ravi on 2019-09-08.
 */
interface ReceivingView {

    fun onFailure(error : Throwable?)

    fun onSuccess(receivingTasks: MutableList<ReceivingDto>)

    fun onShowProgress()

    fun onHideProgress()

}