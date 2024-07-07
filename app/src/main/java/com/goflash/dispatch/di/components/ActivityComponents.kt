package com.goflash.dispatch.di.components

import com.goflash.dispatch.di.module.ActivityModule
import com.goflash.dispatch.di.ActivityScope
import com.goflash.dispatch.features.audit.ui.AuditActivity
import com.goflash.dispatch.features.audit.ui.AuditScanActivity
import com.goflash.dispatch.features.audit.ui.AuditSummaryActivity
import com.goflash.dispatch.features.audit.ui.AuditSummaryDetailActivity
import com.goflash.dispatch.features.bagging.ui.activity.*
import com.goflash.dispatch.features.cash.ui.activity.*
import com.goflash.dispatch.features.dispatch.ui.activity.*
import com.goflash.dispatch.features.lastmile.settlement.ui.activity.*

import com.goflash.dispatch.features.lastmile.tripCreation.ui.activity.*
import com.goflash.dispatch.features.receiving.ui.*
import com.goflash.dispatch.features.receiving.ui.SummaryActivity
import com.goflash.dispatch.features.rtoReceiving.ui.activity.*
import com.goflash.dispatch.ui.activity.*
import com.goflash.dispatch.ui.activity.LoginActivity
import dagger.Component

/**
 * Created by Ravi on 28/05/19.
 */
@ActivityScope
@Component(dependencies = [NetworkComponent::class], modules = [ActivityModule::class])
interface ActivityComponents {

    fun inject(loginActivity: LoginActivity)

    fun inject(sortActivity: SortActivity)

    fun inject(activity: DispatchActivity)

    fun inject(activity: ScanSortedBinActivity)

    fun inject(activity: ScanDispatchBinActivity)

    fun inject(activity: CancelledActivity)

    fun inject(activity: OrdersListActivity)

    fun inject(activity: HomeActivity)

    fun inject(activity: BagDetailActivity)

    fun inject(activity: ScannedShipmentsActivity)

    fun inject(activity: BagListActivity)

    fun inject(activity: DiscardBagActivity)

    fun inject(activity: ReceivingActivity)

    fun inject(activity: VehicleScanActivity)

    fun inject(activity: ReceiveBagActivity)

    fun inject(activity: SummaryActivity)

    fun inject(activity: PreviewActivity)

    fun inject(activity: AddBagsActivity)

    fun inject(activity: DispatchBagActivity)

    fun inject(activity: ScannedBagsActivity)

    fun inject(activity: VehicleDetailActivity)

    fun inject(activity: DispatchVehicleActivity)

    fun inject(activity: InvoiceListActivity)

    fun inject(activity: TripListActivity)

    fun inject(activity: CompleteActivity)

    fun inject(activity: ProfileActivity)

    fun inject(activity: ConsignmentDetailActivity)

    fun inject(activity: AuditActivity)

    fun inject(activity: AuditScanActivity)

    fun inject(activity: AuditSummaryActivity)

    fun inject(activity: AuditSummaryDetailActivity)

    fun inject(activity: RaiseTicketActivity)

    fun inject(activity: UnassignedShipmentActivity)

    fun inject(activity: ScanToSearchActivity)

    fun inject(activity: LastMileActivity)

    fun inject(activity: FilterActivity)

    fun inject(activity: AddShipmentActivity)

    fun inject(activity: ScanShipmentActivity)

    fun inject(activity: SelectSprinterActivity)

    fun inject(activity: LastMileSummaryActivity)

    fun inject(activity: Step1UndeliveredActivity)

    fun inject(activity: Step2PickedUpActivity)

    fun inject(activity: ScanReturnItemActivity)

    fun inject(activity: Step3CashCollectionActivity)

    fun inject(activity: ItemSummaryActivity)

    fun inject(activity: ReviewItemActivity)

    fun inject(activity: ReceiveItemActivity)

    fun inject(activity: SelectCancelReason)

    fun inject(assignZonesActivity: AssignZonesActivity)

    fun inject(activity: RemoveBagsActivity)

    fun inject(activity: CashClosingActivity)

    fun inject(createSummaryActivity: CreateSummaryActivity)

    fun inject(addExpenseActivity: AddExpenseActivity)

    fun inject(addCashPickupActivity: AddCashPickupActivity)

    fun inject(mergeCreatedTripActivity: MergeCreatedTripActivity)

    fun inject(activity: SummaryDetailActivity)

    fun inject(mpsBoxesActivity: MPSBoxesActivity)

    fun inject(activity: ReceiveFmPickupShipmentActivity)

    fun inject(activity: FmSummaryActivity)

    fun inject(activity: FmPickupReviewActivity)

    fun inject(activity: CashCollectionBreakupActivity)

    fun inject(activity: MaintenanceActivity)

    fun inject(activity: ReceiveShipmentsListActivity)

    fun inject(activity: ScanReceiveShipmentActivity)

    fun inject(activity: RunsheetActivity)

    fun inject(activity: ReceiveMpsShipmentListActvity)

    fun inject(activity: ScanBinActivity)

    fun inject(step4VerifyImagesActivity: Step4VerifyImagesActivity)

    fun inject(reviewImagesActivity: ReviewImagesActivity)

    fun inject(approveImageActivity: ApproveImageActivity)

    fun inject(sizeSelectionActivity: SizeSelectionActivity)

    fun inject(runsheetListActivity: RunsheetListActivity)

}