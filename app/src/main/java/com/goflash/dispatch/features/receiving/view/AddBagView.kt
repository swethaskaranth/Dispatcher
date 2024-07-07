package com.goflash.dispatch.features.receiving.view

/**
 *Created by Ravi on 2019-09-08.
 */
interface AddBagView {

    fun onFailure(error : Throwable?)

    fun onSuccess(count: HashSet<String>)

    fun onShowProgress()

    fun onHideProgress()

    fun showMessage(msg: String)

    fun updateBag(count: Int)

    fun onSealRequiedFetched(sealRequired : Boolean)
}