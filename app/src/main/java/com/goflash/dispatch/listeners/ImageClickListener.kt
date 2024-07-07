package com.goflash.dispatch.listeners

interface ImageClickListener {

    fun onImageClicked(position: Int)

    fun onImageRejected(position: Int)

    fun onImageApproved(position: Int)
}