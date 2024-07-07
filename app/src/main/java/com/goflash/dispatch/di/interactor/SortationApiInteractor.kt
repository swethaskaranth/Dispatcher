package com.goflash.dispatch.di.interactor

import com.goflash.dispatch.api_services.SortationServices
import com.goflash.dispatch.data.*
import com.goflash.dispatch.model.*
import com.goflash.dispatch.util.PreferenceHelper
import com.goflash.dispatch.model.VehicleSealRequired
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Part
import retrofit2.http.Url
import rx.Observable
import rx.Single
import javax.inject.Inject

/**
 * Created by Ravi on 28/05/19.
 */
class SortationApiInteractor @Inject constructor(private val sortationService: SortationServices) {

    /**
     * Login Interactor
     */

    fun login(credentials: Credentials): Single<Profile> {
        return sortationService.login(credentials)
    }

    fun logout(credentials: Credentials): Single<ResponseBody> {
        return sortationService.logout(credentials)
    }

    /**
     * Sortation Interactor
     */
    fun getSortationBin(request: CommonRequest): Single<PackageDto> {
        return sortationService.getSortationBin(
            PreferenceHelper.assignedAssetId,
            PreferenceHelper.assignedAssetName,
            request
        )
    }

    fun getSortationBinV3(barcode: String): Single<PackageDto> {
        return sortationService.getSortationBinV3(
            PreferenceHelper.assignedAssetId,
            PreferenceHelper.assignedAssetName,
            barcode
        )
    }

    fun getSortationBinV4(order: SortOrder): Single<PackageDto> {
        return sortationService.getSortationBinV4(
            PreferenceHelper.assignedAssetId,
            PreferenceHelper.assignedAssetName,
            order
        )
    }

    fun updateBin(PackageDto: PackageDto): Single<PackageDto> {
        return sortationService.updateBinV2(
            PreferenceHelper.assignedAssetId,
            PreferenceHelper.assignedAssetName,
            PackageDto
        )
    }

    fun initiateDispatch(barcode: String): Single<PackageDto> {
        return sortationService.initiateDispatch(
            PreferenceHelper.assignedAssetId,
            PreferenceHelper.assignedAssetName, barcode
        )
    }

    fun dispatchShipments(commonRequest: ArrayList<DispatchShipmentRequest>): Single<PackageDto> {
        return sortationService.dispatchShipments(commonRequest)
    }

    fun dispatchShipmentsV2(commonRequest: ArrayList<DispatchShipmentRequest>): Single<PackageDto> {
        return sortationService.dispatchShipmentsV2(commonRequest)
    }

    /**
     * Receiving Interactor
     */
    fun getExpectedTrips(): Single<MutableList<ReceivingDto>> {
        return sortationService.getExpectedTrips(
            PreferenceHelper.assignedAssetId,
            PreferenceHelper.assignedAssetName
        )
    }

    fun uploadImage(
        file: MultipartBody.Part,
        vehicleId: String
    ): Single<MutableList<VehicleDetails>> {
        return sortationService.uploadImage(
            file,
            vehicleId,
            PreferenceHelper.assignedAssetId,
            PreferenceHelper.assignedAssetName
        )
    }

    fun verifiyVehicleSealScan(
        taskId: String?,
        tripId: String?
    ): Single<MutableList<VehicleDetails>> {
        return sortationService.verifyVehicleSealScan(
            taskId,
            tripId,
            PreferenceHelper.assignedAssetId,
            PreferenceHelper.assignedAssetName
        )
    }

    fun receivingComplete(receiveRequest: ReceivingRequest): Single<String> {
        return sortationService.receivingComplete(
            receiveRequest,
            PreferenceHelper.assignedAssetId,
            PreferenceHelper.assignedAssetName
        )
    }

    /**
     * Bagging Interactor
     */
    fun getShipmentsinBin(barcode: String): Single<PackageDto> {
        return sortationService.getShipmentsinBin(barcode)
    }

    fun createBag(bagDto: BagDTO): Single<ResponseBody> {
        return sortationService.createBag(bagDto)
    }

    fun getBagList(assetId: String): Observable<ArrayList<BagDTO>> {
        return sortationService.getBagList(assetId)
    }

    fun getBagDetail(bagId: String): Single<BagDTO> {
        return sortationService.getBagDetail(bagId)
    }

    fun discardBag(bagId: String): Single<ResponseBody> {
        return sortationService.discardBag(bagId)
    }

    fun getSprinterList(): Observable<ArrayList<Sprinter>> {
        return sortationService.getSprinterList(PreferenceHelper.assignedAssetId, "Sprinter")
    }

    fun createBagTrips(bagTripDTO: BagTripDTO): Single<BagTripDTO> {
        return sortationService.createBagTrips(
            PreferenceHelper.assignedAssetId,
            PreferenceHelper.assignedAssetName, bagTripDTO
        )
    }

    fun getInvoiceUrl(request: InvoiceDetailRequest): Single<Invoice> {
        return sortationService.getInvoiceUrlV2(
            PreferenceHelper.assignedAssetId,
            PreferenceHelper.assignedAssetName, request
        )
    }

    fun getInvoiceUrlv2(invoiceId: String): Single<Invoice> {
        return sortationService.getInvoiceUrlV3(
            PreferenceHelper.assignedAssetId,
            PreferenceHelper.assignedAssetName, invoiceId
        )
    }

    fun getInvoiceList(
        from: String? = null,
        to: String? = null,
        tripId: Long? = null
    ): Observable<ArrayList<Invoice>> {
        return sortationService.getInvoiceList(
            PreferenceHelper.assignedAssetId,
            PreferenceHelper.assignedAssetName,
            from,
            to,
            tripId
        )
    }

    fun getTripList(): Observable<ArrayList<ReceivingDto>> {
        return sortationService.getTripList(PreferenceHelper.assignedAssetId)
    }

    fun getSummary(from: String, to: String): Observable<BagTripSummaryDetail> {
        return sortationService.getSummary(
            PreferenceHelper.assignedAssetId,
            PreferenceHelper.assignedAssetName,
            from,
            to
        )
    }

    fun getBagShipments(tripId: Long): Observable<ArrayList<ShipmentCount>> {
        return sortationService.getBagShipmentList(
            PreferenceHelper.assignedAssetId,
            PreferenceHelper.assignedAssetName, tripId
        )
    }

    fun createAudit(): Observable<AuditResponse> {
        return sortationService.createAudit(
            PreferenceHelper.assignedAssetId,
            PreferenceHelper.assignedAssetName
        )
    }

    fun addAuditItems(auditItemsRequest: AuditItemsRequest): Observable<ResponseBody> {
        return sortationService.addAuditItems(auditItemsRequest)
    }

    fun getSummaryList(): Observable<List<AuditHistory>> {
        return sortationService.getAuditList(PreferenceHelper.assignedAssetId)
    }

    fun getSummaryForId(auditId: Long): Observable<AuditSummary> {
        return sortationService.getSummaryForId(auditId)
    }


    fun getVehicleSealRequired(list: List<CommonRequest>): Observable<VehicleSealRequired> {
        return sortationService.getVehicleSealRequired(list)
    }

    fun getCurrentAuditDetails(): Observable<ActiveAudit> {
        return sortationService.getCurrentAuditDetails(PreferenceHelper.assignedAssetId)
    }

    fun uploadFeedbackImage(
        file: MutableList<MultipartBody.Part>,
        title: RequestBody,
        description: RequestBody,
        priority: RequestBody,
        product: RequestBody,
        email: RequestBody,
        assignedAssetName: RequestBody
    ): Single<ResponseBody> {
        return sortationService.uploadFeedbackImage(
            file,
            title,
            description,
            priority,
            product,
            email,
            assignedAssetName
        )
    }

    /**
     * Trip creation APIs Starts from here
     */

    fun getParticularTrips(activeTrips: ActiveTrips): Observable<List<TripDTO>> {
        return sortationService.getParticularTrips(activeTrips)
    }

    fun getSprinters(): Observable<List<SprinterList>> {
        return sortationService.getSprinters(
            PreferenceHelper.assignedAssetId,
            PreferenceHelper.assignedAssetName,
            "SPRINTER"
        )
    }

    fun assignSprinterToTrip(assignSprinter: AssignSprinter): Observable<CommonResponse> {
        return sortationService.assignSprinterToTrip(assignSprinter)
    }

    fun addShipmentToTrip(commonRequest: MutableList<CommonRequest>): Observable<CommonResponse> {
        return sortationService.addShipmentToTrip(
            PreferenceHelper.assignedAssetId,
            PreferenceHelper.assignedAssetName,
            commonRequest
        )
    }

    fun removeShipment(commonRequest: CommonRequest): Observable<CommonResponse> {
        return sortationService.removeShipment(
            PreferenceHelper.assignedAssetId,
            PreferenceHelper.assignedAssetName,
            commonRequest
        )
    }

    fun deleteTrip(deleteTrip: DeleteTrip): Observable<CommonResponse> {
        return sortationService.deleteTrip(
            PreferenceHelper.assignedAssetId,
            PreferenceHelper.assignedAssetName,
            deleteTrip
        )
    }


    fun unassignedShipmentListFilter(activeTrips: ActiveTrips): Observable<List<UnassignedDTO>> {
        return sortationService.unassignedShipmentListFilter(
            PreferenceHelper.assignedAssetId,
            PreferenceHelper.assignedAssetId,
            activeTrips
        )
    }

    fun deleteShipment(deleteShipment: DeleteShipment): Observable<CommonResponse> {
        return sortationService.deleteShipment(deleteShipment)
    }

    fun createManualTrip(sprinterId: String): Observable<TripDTO> {
        return sortationService.createManualTrip(PreferenceHelper.assignedAssetId, sprinterId)
    }

    fun getShipmentsforTrip(tripId: Long): Observable<List<TaskListDTO>> {
        return sortationService.getShipmentsForTrip(tripId)
    }


    fun getTabCount(startDate: String, endDate: String): Observable<List<TripCount>> {
        return sortationService.getTabCount(startDate, endDate)
    }

    fun unAssignedCount(): Observable<UnassignedCount> {
        return sortationService.unAssignedCount()
    }

    fun unBlockShipment(DeleteShipment: DeleteShipment): Observable<CommonResponse> {
        return sortationService.unBlockShipment(DeleteShipment)
    }


    fun getTripSettlementDetails(tripId: Long): Observable<TripSettlementDTO> {
        return sortationService.getTripSettlementDetails(tripId)
    }

    fun getReturnedItens(tripId: Long, request: DispatchShipmentRequest): Observable<List<Item>> {
        return sortationService.getReturnedItems(
            PreferenceHelper.assignedAssetId,
            PreferenceHelper.assignedAssetId,
            tripId,
            request
        )
    }

    fun settleTrip(
        tripId: Long,
        tripSettlementDTO: TripSettlementCompleteDTO
    ): Observable<TripSettlementResponse> {
        return sortationService.settleTripV3(
            PreferenceHelper.assignedAssetId,
            PreferenceHelper.assignedAssetId,
            tripId,
            tripSettlementDTO,
            PreferenceHelper.assignedAssetName
        )
    }

    fun verifyPincode(reason: String, shipmentId: String): Observable<PincodeVerify> {
        return sortationService.verifyPincode(reason, shipmentId)
    }

    fun updatePincode(updatePincode: UpdatePincode): Observable<CommonResponse> {
        return sortationService.updatePincode(updatePincode)
    }

    fun createSmartTrip(request: ZoneSprinterDetailsRequest): Observable<CommonResponse> {
        return sortationService.createSmartTrip(
            PreferenceHelper.assignedAssetId,
            PreferenceHelper.assignedAssetId,
            request
        )
    }

    fun smartTripProcess(): Observable<List<SmartTripResponse>> {
        return sortationService.smartTripProcess(
            PreferenceHelper.assignedAssetId,
            PreferenceHelper.assignedAssetId
        )
    }

    fun cancelSmartTrip(id: Int): Observable<CommonResponse> {
        return sortationService.cancelSmartTrip(
            PreferenceHelper.assignedAssetId,
            PreferenceHelper.assignedAssetId,
            id
        )

    }

    fun getBalances(): Observable<BalancesDTO> {
        return sortationService.getBalances(
            PreferenceHelper.assignedAssetId,
            PreferenceHelper.assignedAssetId
        )
    }

    fun getCashClosingDetails(page: Int, size: Int, entityId: Int): Observable<CashClosingDetails> {
        return sortationService.getCashClosingDetails(
            PreferenceHelper.assignedAssetId,
            PreferenceHelper.assignedAssetId,
            page,
            size,
            entityId
        )
    }

    fun uploadCashImage(file: MultipartBody.Part): Single<ImageUploadDetails> {
        return sortationService.uploadCashImage(file)
    }

    fun createCashSummary(cashRequest: CashClosingRequest): Single<ResponseBody> {
        return sortationService.createCashSummaryV4(
            cashRequest,
            PreferenceHelper.assignedAssetId,
            PreferenceHelper.assignedAssetName
        )
    }

    fun getInTransitTrips(): Single<List<InTransitTrip>> {
        return sortationService.getInTransitTrips(
            PreferenceHelper.assignedAssetId,
            PreferenceHelper.assignedAssetName
        )
    }

    /**
     * Merge Created Trips
     */
    fun mergeCreatedTrips(request: MergeCreatedTripRequest): Single<ResponseBody> {
        return sortationService.mergeCreatedTrips(
            PreferenceHelper.assignedAssetId,
            PreferenceHelper.assignedAssetName,
            request
        )
    }

    /**
     * View Address Details
     */
    fun viewAddressDetails(shipmentId: String): Single<AddressDTO> {
        return sortationService.viewAddressDetails(shipmentId)
    }

    fun getFmPickupShipment(barcode: String, tripId: Long): Single<FmPickedShipment> {
        return sortationService.getFmPickupShipment(barcode, tripId)
    }

    fun createSmartTripV2(request: SmartTripCreateRequest): Observable<CommonResponse> {
        return sortationService.createSmartTripV2(
            PreferenceHelper.assignedAssetId,
            PreferenceHelper.assignedAssetId,
            request
        )
    }

    fun getCashCollectionBreakup(
        cashClosingId: String?,
        page: Int,
        size: Int
    ): Observable<CashCollectionBreakupResponse> {
        return sortationService.getCashCollectionBreakup(
            PreferenceHelper.assignedAssetId,
            cashClosingId,
            page,
            size
        )
    }

    fun updateVehicleNumber(id: String, number: String): Observable<Boolean> {
        return sortationService.updateVehicleNumber(id, number)
    }

    fun checkHealthStatus(): Observable<Boolean> {
        return sortationService.checkHealthStatus()
    }

    fun getInwardRuns(datForNumDays: Int): Observable<InwardRunResponse> {
        return sortationService.getInwardRuns(
            PreferenceHelper.assignedAssetId,
            PreferenceHelper.assignedAssetName,
            datForNumDays
        )
    }

    fun getInwardRunItemsForRunId(runId: Int): Observable<InwardRun> {
        return sortationService.getInwardRunItemsForRunId(
            PreferenceHelper.assignedAssetId,
            PreferenceHelper.assignedAssetName,
            runId
        )
    }

    fun getShipmentDetails(barcode: String, partnerId: Int?): Observable<ReceivingShipmentDTO?> {
        return sortationService.getShipmentDetails(
            PreferenceHelper.assignedAssetId,
            PreferenceHelper.assignedAssetName,
            barcode,
            partnerId
        )
    }

    fun createInwardRun(createInwardRequest: CreateInwardRequest): Observable<InwardRun> {
        return sortationService.createInwardRun(
            PreferenceHelper.assignedAssetId,
            PreferenceHelper.assignedAssetName,
            createInwardRequest
        )
    }

    fun createInwardRunItem(
        runId: Int,
        createInwardRunItemRequest: CreateInwardRunItemRequest
    ): Observable<InwardRunItem> {
        return sortationService.createInwardRunItem(
            PreferenceHelper.assignedAssetId,
            PreferenceHelper.assignedAssetName,
            runId,
            createInwardRunItemRequest
        )
    }

    fun getExceptionReasonsForShipment(shipmentId: Int): Observable<ExceptionReasonResponse> {
        return sortationService.getExceptionReasonsForShipment(
            PreferenceHelper.assignedAssetId,
            PreferenceHelper.assignedAssetName,
            shipmentId
        )
    }

    fun updateInwardRunItem(
        runId: Int,
        runItemId: Int,
        updateInwardRunItemRequest: UpdateInwardRunItemRequest
    ): Observable<ResponseBody> {
        return sortationService.updateInwardRunItem(
            PreferenceHelper.assignedAssetId,
            PreferenceHelper.assignedAssetName,
            runId,
            runItemId,
            updateInwardRunItemRequest
        )
    }

    fun completeInwardRun(
        inwardRunId: Int,
        request: CompleteInwardRunRequest
    ): Observable<ResponseBody> {
        return sortationService.completeInwardRun(
            PreferenceHelper.assignedAssetId,
            PreferenceHelper.assignedAssetName,
            inwardRunId,
            request
        )
    }

    fun getShipmentCountForMpsInwardItems(request: MpsShipmentCountRequest): Observable<List<MpsShipmentCountDTO>> {
        return sortationService.getShipmentCountForMpsInwardItems(
            PreferenceHelper.assignedAssetId,
            PreferenceHelper.assignedAssetName,
            request
        )
    }

    /**
     * @param credentials [ numberLogin credentials to be passed]
     * */
    fun getOtpLogin(credentials: Credentials): Single<Profile> {
        return sortationService.numberSignInV2(credentials)
    }

    /**
     * @param credentials [ numberLogin credentials to be passed]
     * */
    fun otpLogin(credentials: Credentials): Single<Profile> {
        return sortationService.otpLogin(credentials)
    }

    fun getSortationBinForSingleScan(order: SortOrder): Single<PackageDto> {
        return sortationService.getSortationBinForSingleScan(
            PreferenceHelper.assignedAssetId,
            PreferenceHelper.assignedAssetName,
            order
        )
    }

    fun getPreSignedUrl(requestFor: String? = null): Observable<PreSignedUrlResponse> {
        return sortationService.getPreSignedUrlV1(requestFor)
    }

    fun uploadAckSlip(
        contentType: String,
        uploadUrl: String,
        requestBody: RequestBody
    ): Observable<ResponseBody> {
        return sortationService.uploadAckSlip(contentType, uploadUrl, requestBody)
    }

    fun getBagDetailsToAddToTrip(bagId: String): Single<BagDTO> {
        return sortationService.getBagDetailsToAddToTrip(
            PreferenceHelper.assignedAssetId,
            PreferenceHelper.assignedAssetName, bagId
        )
    }

    fun getAdhocCashCollectionBreakup(
        cashClosingId: String?,
        page: Int,
        size: Int
    ): Observable<AdhocCashCollectionBreakupResponse> {
        return sortationService.getAdhocCashCollectionBreakup(
            PreferenceHelper.assignedAssetId,
            cashClosingId,
            page,
            size
        )
    }

    fun getMaxTasksPerSprinter(zoneIds: List<Int>): Observable<Map<Int, Int>> =
        sortationService.getMaxTasksPerSprinter(zoneIds)

    fun getConsolidatedManifest(request: ConsolidatedManifestRequest): Observable<List<MidMileDispatchedRunsheet>> {
        return sortationService.getConsolidatedManifest(
            PreferenceHelper.assignedAssetId,
            PreferenceHelper.assignedAssetName,
            request
        )
    }

    fun closeBin(closeBinRequest: RouteIdBasedTripDto): Observable<ResponseBody> =
        sortationService.closeBin(
            PreferenceHelper.assignedAssetId.toString(),
            PreferenceHelper.assignedAssetId.toString(), closeBinRequest
        )

}
