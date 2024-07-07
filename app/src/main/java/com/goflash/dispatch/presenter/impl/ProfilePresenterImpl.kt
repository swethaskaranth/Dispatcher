package com.goflash.dispatch.presenter.impl

import android.content.Context
import com.goflash.dispatch.api_services.SessionService
import com.goflash.dispatch.di.interactor.SortationApiInteractor
import com.goflash.dispatch.model.Credentials
import com.goflash.dispatch.presenter.UserProfilePresenter
import com.goflash.dispatch.presenter.views.UserView
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription

class ProfilePresenterImpl(private val sortationApiInteractor: SortationApiInteractor) : UserProfilePresenter {

    private var view: UserView? = null

    private var compositeSubscription: CompositeSubscription? = null

    private lateinit var context : Context

    override fun onAttachView(ctx: Context, userView: UserView) {
        this.view = userView
        compositeSubscription = CompositeSubscription()
        context = ctx
    }

    override fun onDetachView() {
        if (view == null)
            return
        view = null
        compositeSubscription?.unsubscribe()
        compositeSubscription = null

    }

    override fun getUserDetails() {
        view?.setViews(SessionService.name)
    }

    override fun logout() {

        compositeSubscription?.add(sortationApiInteractor.logout(Credentials(SessionService.token))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                view?.onSuccessLogout()
            }, {
                view?.onFailure(it)
            }))
    }

}