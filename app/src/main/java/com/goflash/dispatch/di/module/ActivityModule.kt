package com.goflash.dispatch.di.module

import com.goflash.dispatch.di.ActivityScope
import com.goflash.dispatch.di.interactor.SortationApiInteractor
import com.goflash.dispatch.features.audit.presenter.AuditPresenter
import com.goflash.dispatch.features.audit.presenter.AuditScanPresenter
import com.goflash.dispatch.features.audit.presenter.AuditSummaryDetailPresenter
import com.goflash.dispatch.features.audit.presenter.AuditSummaryPresenter
import com.goflash.dispatch.features.audit.presenter.impl.AuditPresenterImpl
import com.goflash.dispatch.features.audit.presenter.impl.AuditScanPresenterImpl
import com.goflash.dispatch.features.audit.presenter.impl.AuditSummaryDetailPresenterImpl
import com.goflash.dispatch.features.audit.presenter.impl.AuditSummaryPresenterImpl
import com.goflash.dispatch.features.bagging.presenter.*
import com.goflash.dispatch.features.bagging.presenter.impl.*
import com.goflash.dispatch.features.cash.presenter.*
import com.goflash.dispatch.features.cash.presenter.impl.*
import com.goflash.dispatch.features.dispatch.presenter.*
import com.goflash.dispatch.features.dispatch.presenter.impl.*
import com.goflash.dispatch.features.lastmile.settlement.presenter.*

import com.goflash.dispatch.features.lastmile.settlement.presenter.impl.*
import com.goflash.dispatch.features.lastmile.tripCreation.presenter.*
import com.goflash.dispatch.features.lastmile.tripCreation.presenter.impl.*
import com.goflash.dispatch.features.receiving.presenter.*
import com.goflash.dispatch.features.receiving.presenter.SummaryPresenter
import com.goflash.dispatch.features.receiving.presenter.impl.*
import com.goflash.dispatch.features.receiving.presenter.impl.SummaryPresenterImpl
import com.goflash.dispatch.features.rtoReceiving.presenter.*
import com.goflash.dispatch.features.rtoReceiving.presenter.impl.*
import com.goflash.dispatch.presenter.*
import com.goflash.dispatch.presenter.impl.*
import dagger.Module
import dagger.Provides

/**
 * Created by Ravi on 28/05/19.
 */
@Module
class ActivityModule {

    @Provides
    @ActivityScope
    fun provideLoginPresenter(sortationApiInteractor: SortationApiInteractor): LoginPresenter {
        return LoginPresenterImpl(sortationApiInteractor)
    }

    @Provides
    @ActivityScope
    fun provideSortPresenter(sortationApiInteractor: SortationApiInteractor): SortPresenter {
        return SortActivityPresenterImpl(sortationApiInteractor)
    }

    @Provides
    @ActivityScope
    fun provideDispatchPresenter(sortationApiInteractor: SortationApiInteractor): DispatchPresenter {
        return DispatchPresenterImpl(sortationApiInteractor)
    }

    @Provides
    @ActivityScope
    fun provideScanSortBinPresenter(sortationApiInteractor: SortationApiInteractor): ScanSotrBinPresenter {
        return ScanSortBinPresenterImpl(sortationApiInteractor)
    }

    @Provides
    @ActivityScope
    fun provideScanDispatchBinPresenter(sortationApiInteractor: SortationApiInteractor): ScanDispatchBinPresenter {
        return ScanDispatchBinPresenterImpl(sortationApiInteractor)
    }

    @Provides
    @ActivityScope
    fun provideCancelledPresenter(sortationApiInteractor: SortationApiInteractor): CancelledPresenter {
        return CancelledPresenterImpl(sortationApiInteractor)
    }

    @Provides
    @ActivityScope
    fun provideOrderListPresenter(): OrderListPresenter {
        return OrderListPresenterImpl()
    }

    @Provides
    @ActivityScope
    fun provideMainPresenter(sortationApiInteractor: SortationApiInteractor): MainPrsenter {
        return MainPresenterImpl(sortationApiInteractor)
    }

    @Provides
    @ActivityScope
    fun provideHomePresenter(sortationApiInteractor: SortationApiInteractor): HomePresenter {
        return HomePresenterImpl(sortationApiInteractor)
    }

    @Provides
    @ActivityScope
    fun provideBagDetailPresenter(sortationApiInteractor: SortationApiInteractor): BagDetailPresenter {
        return BagDetailPresenterImpl(sortationApiInteractor)
    }

    @Provides
    @ActivityScope
    fun provideReceivingPresenter(sortationApiInteractor: SortationApiInteractor): ReceivingPresenter {
        return ReceivingPresenterImpl(sortationApiInteractor)
    }

    @Provides
    @ActivityScope
    fun provideBagListPresenter(sortationApiInteractor: SortationApiInteractor): BagListPresenter {
        return BagListPresenterImpl(sortationApiInteractor)
    }

    @Provides
    @ActivityScope
    fun providePreviewPresenter(sortationApiInteractor: SortationApiInteractor): PreviewPresenter {
        return PreviewPresenterImpl(sortationApiInteractor)
    }

    @Provides
    @ActivityScope
    fun provideDiscardBagPresenter(sortationApiInteractor: SortationApiInteractor): DiscardBagPresenter {
        return DiscardBagPresenterImpl(sortationApiInteractor)
    }

    @Provides
    @ActivityScope
    fun provideReceiveBagPresenter(sortationApiInteractor: SortationApiInteractor): ReceiveBagsPresenter {
        return ReceiveBagsPresenterImpl(sortationApiInteractor)
    }

    @Provides
    @ActivityScope
    fun provideDispatchBagPresenter(sortationApiInteractor: SortationApiInteractor): DispatchBagPresenter {
        return DispatchBagPresenterImpl(sortationApiInteractor)
    }

    @Provides
    @ActivityScope
    fun provideScannedBagsPresenter(sortationApiInteractor: SortationApiInteractor): ScannedBagsPresenter {
        return ScannedBagsPresenterImpl(sortationApiInteractor)
    }

    @Provides
    @ActivityScope
    fun provideVehicleDetailPresenter(sortationApiInteractor: SortationApiInteractor): VehicleDetailPresenter {
        return VehicleDetailPresenterImpl(sortationApiInteractor)
    }

    @Provides
    @ActivityScope
    fun provideDispatchVehiclePresenter(sortationApiInteractor: SortationApiInteractor): DispatchVehiclePresenter {
        return DispatchVehiclePresenterImpl(sortationApiInteractor)
    }

    @Provides
    @ActivityScope
    fun provideScannedShipmentsPresenter(sortationApiInteractor: SortationApiInteractor): ScannedShipmentsPresenter {
        return ScannedShipmentsPresenterImpl(sortationApiInteractor)
    }

    @Provides
    @ActivityScope
    fun provideSummaryPresenter(sortationApiInteractor: SortationApiInteractor): SummaryPresenter {
        return SummaryPresenterImpl(sortationApiInteractor)
    }

    @Provides
    @ActivityScope
    fun provideInvoiceListPresenter(sortationApiInteractor: SortationApiInteractor): InvoiceListPresenter {
        return InvoiceListPresenterImpl(sortationApiInteractor)
    }

    @Provides
    @ActivityScope
    fun provideVehiclePresenter(sortationApiInteractor: SortationApiInteractor): VehicleScanPresenter {
        return VehicleScanPresenterImpl(sortationApiInteractor)
    }

    @Provides
    @ActivityScope
    fun provideAddBagPresenter(sortationApiInteractor: SortationApiInteractor): AddBagPresenter {
        return AddBagPresenterImpl(sortationApiInteractor)
    }

    @Provides
    @ActivityScope
    fun provideTripListPresenter(sortationApiInteractor: SortationApiInteractor): TripListPresenter {
        return TripListPresenterImpl(sortationApiInteractor)
    }

    @Provides
    @ActivityScope
    fun provideCompletePresenter(sortationApiInteractor: SortationApiInteractor): CompletePresenter {
        return CompletePresenterImpl(sortationApiInteractor)
    }

    @Provides
    @ActivityScope
    fun provideProfilePresenter(sortationApiInteractor: SortationApiInteractor): UserProfilePresenter {
        return ProfilePresenterImpl(sortationApiInteractor)
    }

    @Provides
    @ActivityScope
    fun provideConsignmentDetailPresenter(sortationApiInteractor: SortationApiInteractor): ConsignmentDetailPresenter {
        return ConsignmentDetailPresenterImpl(sortationApiInteractor)
    }

    @Provides
    @ActivityScope
    fun provideAuditPresenter(sortationApiInteractor: SortationApiInteractor): AuditPresenter {
        return AuditPresenterImpl(sortationApiInteractor)
    }

    @Provides
    @ActivityScope
    fun provideAuditScanPresenter(sortationApiInteractor: SortationApiInteractor): AuditScanPresenter {
        return AuditScanPresenterImpl(sortationApiInteractor)
    }

    @Provides
    @ActivityScope
    fun provideAuditSummaryPresenter(sortationApiInteractor: SortationApiInteractor): AuditSummaryPresenter {
        return AuditSummaryPresenterImpl(sortationApiInteractor)
    }

    @Provides
    @ActivityScope
    fun provideAuditSummaryDetailPresenter(sortationApiInteractor: SortationApiInteractor): AuditSummaryDetailPresenter {
        return AuditSummaryDetailPresenterImpl(sortationApiInteractor)
    }

    @Provides
    @ActivityScope

    fun provideRaiseTicketPresenter(sortationApiInteractor: SortationApiInteractor): RaiseTicketPresenter {
        return RaiseTicketPresenterImpl(sortationApiInteractor)
    }

    @Provides
    @ActivityScope
    fun provideUnassignedPresenter(sortationApiInteractor: SortationApiInteractor): UnassignedShipmentPresenter {
        return UnassignedPresenterImpl(sortationApiInteractor)
    }

    @Provides
    @ActivityScope
    fun provideScanToSearchPresenter(sortationApiInteractor: SortationApiInteractor): ScanToSearchPresenter {
        return ScanToSearchPresenterImpl(sortationApiInteractor)
    }

    @Provides
    @ActivityScope
    fun provideLastMilePresenter(sortationApiInteractor: SortationApiInteractor): LastMilePresenter {
        return LastMilePresenterImpl(
            sortationApiInteractor
        )
    }

    @Provides
    @ActivityScope
    fun provideFilterPresenter(sortationApiInteractor: SortationApiInteractor): FilterPresenter {
        return FilterPresenterImpl(sortationApiInteractor)
    }

    @Provides
    @ActivityScope
    fun provideAddShipmentPresenter(sortationApiInteractor: SortationApiInteractor): AddShipmentPresenter {
        return AddShipmentPresenterImpl(sortationApiInteractor)
    }

    @Provides
    @ActivityScope
    fun provideScanShipmentPresenter(sortationApiInteractor: SortationApiInteractor): ScanShipmentPresenter {
        return ScanShipmentPresenterImpl(sortationApiInteractor)
    }

    @Provides
    @ActivityScope
    fun provideSelectSprinterPresenter(sortationApiInteractor: SortationApiInteractor): SelectSprinterPresenter {
        return SelectSprinterPresenterImpl(sortationApiInteractor)
    }

    @Provides
    @ActivityScope
    fun provideLastMileSummaryPresenter(sortationApiInteractor: SortationApiInteractor): LastMileSummaryPresenter {
        return LastMileSummaryPresenterImpl(sortationApiInteractor)
    }

    @Provides
    @ActivityScope
    fun provideTripSettlementPresenter(sortationApiInteractor: SortationApiInteractor): Step1Presenter {
        return Step1PresenterImpl(
            sortationApiInteractor
        )
    }

    @Provides
    @ActivityScope
    fun provideSettlementShipmentPresenter(sortationApiInteractor: SortationApiInteractor): SettlementScanShipmentPresenter {
        return SettlementScanShipmentPresenterImpl(sortationApiInteractor)
    }

    @Provides
    @ActivityScope
    fun provideScanReturnItemPresenter(sortationApiInteractor: SortationApiInteractor): ScanReturnItemPresenter {
        return ScanReturnItemPresenterImpl(sortationApiInteractor)
    }

    @Provides
    @ActivityScope
    fun provideStep3Presenter(sortationApiInteractor: SortationApiInteractor): Step3Presenter {
        return Step3PresenterImpl(sortationApiInteractor)
    }

    @Provides
    @ActivityScope
    fun provideItemSummaryPresenter(sortationApiInteractor: SortationApiInteractor): ItemSummaryPresenter {
        return ItemSummaryPresenterImpl(sortationApiInteractor)
    }

    @Provides
    @ActivityScope
    fun provideReviewItemPresenter(sortationApiInteractor: SortationApiInteractor): ReviewItemPresenter {
        return ReviewItemPresenterImpl(sortationApiInteractor)
    }

    @Provides
    @ActivityScope
    fun provideReceiveItemPresenter(sortationApiInteractor: SortationApiInteractor): ReceiveItemPresenter {
        return ReceiveItemPresenterImpl(sortationApiInteractor)
    }

    @Provides
    @ActivityScope
    fun provideCashClosingPresenter(sortationApiInteractor: SortationApiInteractor): CashPresenter {
        return CashPresenterImpl(sortationApiInteractor)
    }
    @Provides
    @ActivityScope
    fun provideSelectReasonPresenter(sortationApiInteractor: SortationApiInteractor): SelectReasonPresenter {
        return SelectReasonPresenterImpl(sortationApiInteractor)
    }

    @Provides
    @ActivityScope
    fun provideAssignZonePresenter(sortationApiInteractor: SortationApiInteractor): AssignZonePresenter {
        return AssignZonePresenterImpl(sortationApiInteractor)
    }

    @Provides
    @ActivityScope
    fun provideCreateSummaryPresenter(sortationApiInteractor: SortationApiInteractor): CreateSummaryPresenter {
        return CreateSummaryPresenterImpl(sortationApiInteractor)
    }

    @Provides
    @ActivityScope
    fun provideAddExpensePresenter(sortationApiInteractor: SortationApiInteractor): AddExpensePresenter {
        return AddExpensePresenterImpl(sortationApiInteractor)
    }

    @Provides
    @ActivityScope
    fun provideAddCashPickupPresenter(sortationApiInteractor: SortationApiInteractor): AddCashPickupPresenter {
        return AddCashPickupPresenterImpl(sortationApiInteractor)
    }

    @Provides
    @ActivityScope
    fun provideRemovePresenter(sortationApiInteractor: SortationApiInteractor): RemovedBagsPresenter {
        return RemovedBagsPresenterImpl(sortationApiInteractor)
    }

    @Provides
    @ActivityScope
    fun provideMergeCreatedTripPresenter(sortationApiInteractor: SortationApiInteractor): MergeCreatedTripPresenter {
        return MergeCreatedTripPresenterImpl(sortationApiInteractor)
    }

    @Provides
    @ActivityScope
    fun provideSummaryDetailPresenter(sortationApiInteractor: SortationApiInteractor): SummaryDetailPresenter {
        return SummaryDetailPresenterImpl(sortationApiInteractor)
    }

    @Provides
    @ActivityScope
    fun provideMPSBoxesPresenter(sortationApiInteractor: SortationApiInteractor): MPSBoxesPresenter {
        return MPSBoxesPresenterImpl(sortationApiInteractor)
    }

    @Provides
    @ActivityScope
    fun provideReceiveFmPickupPresenter(sortationApiInteractor: SortationApiInteractor): ReceiveFmPickupPresenter {
        return ReceiveFmPickupPresenterImpl(sortationApiInteractor)
    }

    @Provides
    @ActivityScope
    fun provideFmPickupSummaryPresenter(sortationApiInteractor: SortationApiInteractor): FmSummaryPresenter {
        return FmSummaryPresenterImpl(sortationApiInteractor)
    }

    @Provides
    @ActivityScope
    fun provideFmReviewPresenter(sortationApiInteractor: SortationApiInteractor): FmPickupReviewPresenter {
        return FmPickupReviewPresenterImpl(sortationApiInteractor)
    }

    @Provides
    @ActivityScope
    fun provideMaintenancePresenter(sortationApiInteractor: SortationApiInteractor): MaintenancePresenter {
        return MaintenancePresenterImpl(sortationApiInteractor)
    }

    @Provides
    @ActivityScope
    fun provideReceiveShipmentsListPresenter(sortationApiInteractor: SortationApiInteractor): ReceiveShipmentListPresenter {
        return ReceiveShipmentListPresenterImpl(sortationApiInteractor)
    }

    @Provides
    @ActivityScope
    fun provideScanReceiveShipmentPresenter(sortationApiInteractor: SortationApiInteractor): ScanReceiveShipmentPresenter {
        return ScanReceiveShipmentPresenterImpl(sortationApiInteractor)
    }

    @Provides
    @ActivityScope
    fun provideRunsheetPresenter(sortationApiInteractor: SortationApiInteractor): RunsheetPresenter {
        return RunsheetPresenterImpl(sortationApiInteractor)
    }

    @Provides
    @ActivityScope
    fun provideReceivingMpsShipmentListPresenter(sortationApiInteractor: SortationApiInteractor): ReceiveMpsShipmentListPresenter {
        return ReceiveMpsShipmentListPresenterImpl(sortationApiInteractor)
    }

    @Provides
    @ActivityScope
    fun provideReceivingScanBinPresenter(sortationApiInteractor: SortationApiInteractor): ScanBinPresenter {
        return ScanBinPresenterImpl(sortationApiInteractor)
    }

    @Provides
    @ActivityScope
    fun provideVerifyImagesPresenter(sortationApiInteractor: SortationApiInteractor): VerifyImagesPresenter {
        return VerifyImagesPresenterImpl(sortationApiInteractor)
    }

    @Provides
    @ActivityScope
    fun provideReviewImagesPresenter(sortationApiInteractor: SortationApiInteractor): ReviewImagesPresenter{
        return ReviewImagesPresenterImpl(sortationApiInteractor)
    }

    @Provides
    @ActivityScope
    fun provideApproveImagePresenter(): ApproveImagePresenter{
        return ApproveImagePresenterImpl()
    }

    @Provides
    @ActivityScope
    fun provideSizeSelectionPresenter(): SizeSelectionPresenter{
        return SizeSelectionPresenterImpl()
    }

    @Provides
    @ActivityScope
    fun provideRunsheetListPresenter(sortationApiInteractor: SortationApiInteractor): RunsheetListPresenter{
        return RunsheetListPresenterImpl(sortationApiInteractor)
    }
}