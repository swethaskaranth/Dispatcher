package com.goflash.dispatch.presenter.views

/**
 * Created by Ravi on 14/03/19.
 *
 */
interface RaiseTicketView {

    /**
     * @param error  [ login failure  exceptions]
     * */
    fun onFailure(error: Throwable?)

    /**
     * @param profile  [ feedback success ]
     * */
    fun onSuccess()

}