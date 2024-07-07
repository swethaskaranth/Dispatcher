package com.goflash.dispatch.presenter.impl

import android.content.Context
import android.content.Intent
import co.uk.rushorm.core.RushCore
import co.uk.rushorm.core.RushSearch
import com.goflash.dispatch.R
import com.goflash.dispatch.app_constants.isDispatchable
import com.goflash.dispatch.app_constants.scannedPackage
import com.goflash.dispatch.app_constants.single_order
import com.goflash.dispatch.data.PackageDto
import com.goflash.dispatch.data.ScannedOrder
import com.goflash.dispatch.di.interactor.SortationApiInteractor
import com.goflash.dispatch.presenter.CancelledPresenter
import com.goflash.dispatch.presenter.views.CancelledRowView
import com.goflash.dispatch.presenter.views.CancelledView
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription

class CancelledPresenterImpl(private val sortationApiInteractor: SortationApiInteractor) :
    CancelledPresenter {

    private val TAG = CancelledPresenterImpl::class.java.simpleName

    private var cancelledView: CancelledView? = null

    private var compositeSubscription: CompositeSubscription? = null

    private var allScanned: Boolean = false

    private var sortationPackage = true

    private var singleOrderscanned: Boolean = false

    private var cancelledPackage: PackageDto? = null

    private var cancelledOrders: MutableList<ScannedOrder> = mutableListOf()
    private var orders: MutableList<ScannedOrder> = mutableListOf()

    private var context: Context? = null

    override fun onAttachView(context: Context, view: CancelledView) {
        this.context = context
        this.cancelledView = view
        compositeSubscription = CompositeSubscription()

    }

    override fun onDetachView() {
        if (this.cancelledView == null)
            return
        cancelledView = null
        compositeSubscription?.unsubscribe()
        compositeSubscription = null

    }

    override fun sendIntent(intent: Intent) {
        if (intent.hasExtra(scannedPackage))
            cancelledPackage = intent.getParcelableExtra<PackageDto>(scannedPackage) as PackageDto

        if (intent.hasExtra(single_order))
            singleOrderscanned = intent.getBooleanExtra(single_order, false)

        if (cancelledPackage == null)
            sortationPackage = false

        getNonDispatchableOrders()
    }

    override fun onBinScan(PackageDto: PackageDto) {
        compositeSubscription?.add(
            sortationApiInteractor.updateBin(PackageDto)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    /**
                     * Create one method
                     */
                    if (sortationPackage)
                        cancelledView?.finishSortationTask()
                    else {
                        if (isDispatchableOrderFound()) {
                            RushSearch().whereEqual(isDispatchable, false)
                                .find(ScannedOrder::class.java) { list ->
                                    RushCore.getInstance().delete(list)
                                    cancelledView?.takeToScanActivity()
                                }
                        } else {
                            RushCore.getInstance().clearDatabase()
                            cancelledView?.takeToMainActivity()
                        }
                    }

                }, { error: Throwable? ->
                    cancelledView?.onFailure(error)
                })
        )
    }

    private fun isDispatchableOrderFound(): Boolean {
        val dispatchableOrders =
            RushSearch().whereEqual(isDispatchable, true).find(ScannedOrder::class.java)
        return (dispatchableOrders != null && dispatchableOrders.size > 0)
    }

    override fun onBarcodeScanned(barcode: String) {

        if (allScanned || sortationPackage || singleOrderscanned) {
            if (cancelledOrders[0].binNumber == barcode)
                onBinScan(PackageDto(ArrayList(cancelledOrders), false))
            else
                cancelledView?.onFailure(Throwable("Invalid Barcode"))
        } else {

            markCancelledOrderScanned(barcode)
        }

    }

    private fun markCancelledOrderScanned(barcode: String) {

        allScanned = true

        for (order in cancelledOrders) {
            if (order.packageId == barcode || order.lbn == barcode || order.referenceId == barcode)
                order.isScanned = true
            if (!order.isScanned)
                allScanned = false
        }

        cancelledView?.refreshList()

        if (allScanned)
            cancelledView?.showScanBin(cancelledOrders.get(0).binNumber)
    }


    override fun getNonDispatchableOrders() {
        if (sortationPackage)
            cancelledOrders = cancelledPackage!!.scannedOrders
        else {
            cancelledOrders =
                RushSearch().whereEqual(isDispatchable, false).find(ScannedOrder::class.java)
        }
        //orders = RushSearch().find(ScannedOrder::class.java)
        cancelledView?.showOrHideScanLabel(
            (sortationPackage || singleOrderscanned),
            cancelledOrders[0].status,
            cancelledOrders[0].binNumber
        )

    }

    override fun onBindCanceeldRowView(position: Int, cancelledRowView: CancelledRowView) {
        val order = cancelledOrders[position]

        if (order.packageId != null && order.packageId.isNotEmpty())
            cancelledRowView.setOrderId(
                String.format(
                    context!!.getString(R.string.package_id_label),
                    order.packageId
                )
            )
        else
            cancelledRowView.setOrderId(
                String.format(
                    context!!.getString(R.string.reference_id_label),
                    order.referenceId
                )
            )
        cancelledRowView.setLBN(String.format(context!!.getString(R.string.lbn_label), order.lbn))
        if (order.colourCode != null && order.colourCode.isNotEmpty())
            cancelledRowView.setBackgroundColor("#${order.colourCode}")

        cancelledRowView.checkOrUncheck(order.isScanned || sortationPackage || singleOrderscanned)

    }

    override fun getCount(): Int {
        return cancelledOrders.size
    }


}