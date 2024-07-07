package com.goflash.dispatch.presenter.impl

import android.app.Activity
import android.content.Context
import co.uk.rushorm.core.RushCore
import co.uk.rushorm.core.RushSearch
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.goflash.dispatch.api_services.SessionService
import com.goflash.dispatch.model.Credentials
import com.goflash.dispatch.data.PackageDto
import com.goflash.dispatch.di.interactor.SortationApiInteractor
import com.goflash.dispatch.presenter.MainPrsenter
import com.goflash.dispatch.presenter.views.MainView
import com.goflash.dispatch.util.PreferenceHelper
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription

class MainPresenterImpl(private val sortationApiInteractor: SortationApiInteractor) : MainPrsenter {

    private val TAG = MainPresenterImpl::class.java

    private var mainView: MainView? = null

    private var compositeSubscription: CompositeSubscription? = null

    private lateinit var mGoogleSignInClient: GoogleSignInClient

    override fun onAttachView(context: Context, view: MainView) {
        this.mainView = view
        this.compositeSubscription = CompositeSubscription()
    }

    override fun onDetachView() {
        if (mainView == null)
            return
        mainView = null
        compositeSubscription?.unsubscribe()
        compositeSubscription = null
    }

    override fun checkIfDispatchStarted() {
        val packageDto = RushSearch().findSingle(PackageDto::class.java)

        if (packageDto != null)
            if (isPackageDispatchable(packageDto))
                mainView?.takeToDispatchBinSCreen(packageDto, isPackageDispatchable(packageDto))
            else
                mainView?.takeToCancelledScreen()
    }

    private fun isPackageDispatchable(packageDto: PackageDto): Boolean {

        val nonDispatchableList = packageDto.scannedOrders.filter { order -> !order.isDispatchable }

        /*for (order in packageDto.scannedOrders)
            if (!order.isDispatchable)
                dispatchable = false*/

        return (nonDispatchableList.isEmpty())
    }

    override fun onLogout(activity: Activity, server_client_id: String) {

        compositeSubscription?.add(sortationApiInteractor.logout(
            Credentials(
                ""
            )
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(server_client_id)
                    .requestEmail()
                    .build()

                mGoogleSignInClient = GoogleSignIn.getClient(activity, gso)

                SessionService.token = ""
                PreferenceHelper.token = ""

                RushCore.getInstance().clearDatabase()

                mGoogleSignInClient.signOut().addOnCompleteListener {
                    mainView?.onLogoutSuccesful()

                }
            }, { error ->
                mainView?.onFailure(error)
            })
        )


    }

}