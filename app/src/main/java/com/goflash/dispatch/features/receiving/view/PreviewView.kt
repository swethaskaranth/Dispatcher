package com.goflash.dispatch.features.receiving.view

/**
 *Created by Ravi on 2019-09-08.
 */
interface PreviewView {

    fun onFailure(error : Throwable?)

    fun onSuccess()

    fun onShowProgress()

    fun onHideProgress()

    fun showImagePath(path: String)
}