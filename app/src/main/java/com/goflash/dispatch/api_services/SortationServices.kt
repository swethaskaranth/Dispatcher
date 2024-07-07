package com.goflash.dispatch.api_services

import com.goflash.dispatch.data.*
import com.goflash.dispatch.model.*
import com.goflash.dispatch.model.VehicleSealRequired
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.*
import rx.Observable
import rx.Single

/**
 * Created by Ravi on 28/05/19.
 */
interface SortationServices {

    @POST("user-service/auth/signIn")
    fun login(@Body credentials: Credentials): Single<Profile>

    /**
     * logout api with user-service
     * @param credentials [ Credentials to be passed for logged out]
     * */
    @POST("user-service/auth/signOut")
    fun logout(@Body credentials: Credentials): Single<ResponseBody>

    @POST("sortation-service/sortation/v2/getBin")
    fun getSortationBin(@Header("assetId") aseetId : Long, @Header("assetName") assetName : String,@Body request: CommonRequest): Single<PackageDto>

    @POST("sortation-service/sortation/v3/getBin/{barcode}")
    fun getSortationBinV3(@Header("assetId") aseetId : Long, @Header("assetName") assetName : String,@Path("barcode") barcode: String): Single<PackageDto>

    @POST("sortation-service/sortation/v4/getBin")
    fun getSortationBinV4(@Header("assetId") aseetId : Long, @Header("assetName") assetName : String,@Body order : SortOrder): Single<PackageDto>

    /*@POST("sortation-service/sortation/updateBin")
    fun updateBin(@Header("assetId") assetId : Long, @Header("assetName") assetName : String,@Body PackageDto: PackageDto): Single<PackageDto>*/

    @POST("sortation-service/sortation/v2/updateBin")
    fun updateBinV2(@Header("assetId") assetId : Long, @Header("assetName") assetName : String,@Body PackageDto: PackageDto): Single<PackageDto>

    @GET("dispatch-handler-service/dispatch/initiateDispatch")
    fun initiateDispatch(@Header("assetId") assetId : Long, @Header("assetName") assetName : String,@Query("barcode") barcode : String): Single<PackageDto>

    @POST("dispatch-handler-service/dispatch/dispatchShipments")
    fun dispatchShipments(@Body commonRequest : ArrayList<DispatchShipmentRequest>): Single<PackageDto>

    @POST("dispatch-handler-service/dispatch/v2/dispatchShipments")
    fun dispatchShipmentsV2(@Body commonRequest : ArrayList<DispatchShipmentRequest>): Single<PackageDto>

    /**

     * Receiving {vehicle details}
     */
    @GET("trip-service/trips/expectedTrips")
    fun getExpectedTrips(@Header("assetId") assetId : Long, @Header("assetName") assetName : String): Single<MutableList<ReceivingDto>>

    /**
     * Receiving {vehicle seal scan}
     */
    @Multipart
    @POST("trip-service/trips/vehicleDetailWithImage")
    fun uploadImage(@Part file: MultipartBody.Part, @Header("vehicleId") vehicleId: String, @Header("assetId") assetId : Long,
                          @Header("assetName") assetName : String): Single<MutableList<VehicleDetails>>

    /**
     * Receiving {complete receiving task}
     */
    @POST("trip-service/trips/vehicleDetail")
    fun verifyVehicleSealScan(@Header("vehicleId") vehicleId : String?,@Header("tripId") tripId : String?, @Header("assetId") assetId : Long,
                          @Header("assetName") assetName : String): Single<MutableList<VehicleDetails>>

    /**
     *
     *
     * Receiving {complete receiving task}
     */
    @POST("trip-service/trips/updateBagTrip")
    fun receivingComplete(@Body bagRequest : ReceivingRequest, @Header("assetId") assetId : Long,
                          @Header("assetName") assetName : String): Single<String>

    /**
     * API to get shipments in the scanned bin
     */
    @GET("dispatch-handler-service/dispatch/getBinDetailsForBag")
    fun getShipmentsinBin(@Query("barcode") barcode : String): Single<PackageDto>

    /**
     * API to create a bag
     */
    @POST("trip-handler/bagging/v2/createBag")
    fun createBag(@Body bagDto: BagDTO) : Single<ResponseBody>

    @GET("trip-handler/bagging/bagList")
    fun getBagList(@Query("assetId") assetId : String) : Observable<ArrayList<BagDTO>>

    /**
     * Get Bag Detail by ID
     * @param bagId
     */
    @GET("trip-handler/bagging/bagDetails")
    fun getBagDetail(@Query("bagId") bagId : String) : Single<BagDTO>

    /**
     * API to discard a bag
     * @param bagId
     */
    @POST("trip-handler/bagging/deleteBag")
    fun discardBag(@Query("bagId") bagId : String) : Single<ResponseBody>

    /**
     * Get list of Sprinters for a given asset
     */
    @GET("trip-handler/users/list")
    fun getSprinterList(@Header("assetId") assetId : Long, @Query("type") type : String) : Observable<ArrayList<Sprinter>>

    /**
     * Create a trip for the bags scanned
     */
    @POST("trip-handler/trips/createBagTrips")
    fun createBagTrips(@Header("assetId")assetId: Long, @Header("assetName")assetName: String,@Body bagTripDTO: BagTripDTO) : Single<BagTripDTO>

    /**
     * get url invoice to print
     */
    @GET("trip-handler/invoice/generate")
    fun getInvoiceUrl(@Header("assetId") assetId : Long, @Header("assetName") assetName : String,@Query("tripId") tripId : String) : Single<Invoice>

    /**
     * Get list of invoices
     */
    @GET("trip-handler/invoice/invoiceList")
    fun getInvoiceList(@Query("assetId") assetId : Long, @Header("assetName") assetName : String, @Query("from") from : String?, @Query("to") to : String?, @Query("tripId") tripId: Long? ) : Observable<ArrayList<Invoice>>

    /**
     * Get list of trips from the given asset
     */
    @GET("trip-handler/trips/bagTripListing")
    fun getTripList(@Header("assetId") assetId : Long) : Observable<ArrayList<ReceivingDto>>

    /**
     * Get the summary of bags and trips
     */
    @GET("trip-handler/trips/bagTripSummary")
    fun getSummary(@Header("assetId") assetId : Long, @Header("assetName") assetName : String,@Query("from") from : String, @Query("to") to : String) : Observable<BagTripSummaryDetail>

    /**
     * Get List of Bag IDs and Shipment Count for a given trip
     * @param tripId
     */
    @GET("trip-service/trips/tripBagDetails")
    fun getBagShipmentList(@Header("assetId") assetId : Long, @Header("assetName") assetName : String,@Query("tripId")tripId : Long) : Observable<ArrayList<ShipmentCount>>

    /**
     * create an audit
     */
    @POST("trip-handler/audit/create")
    fun createAudit(@Query("assetId") assetId : Long, @Query("assetName") assetName : String) : Observable<AuditResponse>

    /**
     * Add audit items
     */
    @POST("trip-handler/audit/add_audit_items")
    fun addAuditItems(@Body auditItemsRequest: AuditItemsRequest) : Observable<ResponseBody>

    /**
     * get Audit Summary List
     */
    @GET("trip-handler/audit/list")
    fun getAuditList(@Query("assetId") assetId : Long) : Observable<List<AuditHistory>>

    /**
     * getsummary for
     * @param auditId
     */
    @GET("trip-handler/audit/summary")
    fun getSummaryForId(@Query("auditId")auditId : Long) : Observable<AuditSummary>

    /**
     * get if Vehicle Seal is required
     */
    @POST("trip-handler/bagging/additionalDetailForBagTrip")
    fun getVehicleSealRequired(@Body list : List<CommonRequest>) : Observable<VehicleSealRequired>

     /* Get Current active audit details
     */
    @GET("shipment-service/audit/activeAudit")
    fun getCurrentAuditDetails(@Query("assetId") assetId : Long) : Observable<ActiveAudit>

    /**
     * Receiving {upload feedback image}
     */
    @Multipart
    @POST("trip-handler/ticket/createV2")
    fun uploadFeedbackImage(@Part screenshot: MutableList<MultipartBody.Part>, @Part("title") title: RequestBody, @Part("description") description : RequestBody,
                            @Part("priority") priority : RequestBody, @Part("product") product : RequestBody, @Part("cc") cc : RequestBody,
                            @Part("assetName") assetName: RequestBody
    ): Single<ResponseBody>

    /**
     * Trip creation APIs Starts from here
     */

    /**
     * Get Current active audit details
     */
    @POST("trip-handler/trips/list")
    fun getParticularTrips(@Body activeTrips: ActiveTrips) : Observable<List<TripDTO>>

    /**
     * Get Current active audit details
     */
    @GET("trip-handler/users/list")
    fun getSprinters(@Header("assetId") assetId : Long, @Header("assetName") assetName : String,@Query("type") type: String) : Observable<List<SprinterList>>

    /**
     * Get Current active audit details
     */
    @POST("trip-handler/trips/assign/sprinter")
    fun assignSprinterToTrip(@Body assignSprinter: AssignSprinter) : Observable<CommonResponse>

    /**
     * Get Current active audit details
     */
    @POST("trip-handler/trips/add_shipment")
    fun addShipmentToTrip(@Header("assetId") assetId : Long, @Header("assetName") assetName : String,@Body commonRequest: MutableList<CommonRequest>) : Observable<CommonResponse>

    /**
     * Get Current active audit details
     */
    @POST("trip-handler/trips/remove_shipment")
    fun removeShipment(@Header("assetId") assetId : Long, @Header("assetName") assetName : String,@Body commonRequest: CommonRequest) : Observable<CommonResponse>

    /**
     * Get Current active audit details
     */
    @POST("trip-handler/trips/unassign")
    fun deleteTrip(@Header("assetId") assetId : Long, @Header("assetName") assetName : String,@Body deleteTrip: DeleteTrip) : Observable<CommonResponse>

    /**
     * Get Current active audit details
     */
    @POST("trip-handler/shipments/v1/unassigned")
    fun unassignedShipmentListFilter(@Header("assetId") assetId : Long, @Header("assetName") assetName : Long,@Body activeTrips: ActiveTrips) : Observable<List<UnassignedDTO>>


    /**
     * Get Current active audit details
     */
    @POST("trip-handler/trips/task/undelivered")
    fun deleteShipment(@Body DeleteShipment: DeleteShipment) : Observable<CommonResponse>

    /**
     * Create Manual Trip
     */
    @JvmSuppressWildcards
    @GET("trip-handler/trips/v1/create/manual")
    fun createManualTrip(@Header("assetId") assetId : Long,@Header("sprinterId") sprinterId : String) : Observable<TripDTO>

    /**
     * Get shipments for
     * @param tripId
     */
    @POST("trip-handler/trips/{tripId}/task/list")
    fun getShipmentsForTrip(@Path("tripId") tripId : Long) : Observable<List<TaskListDTO>>


    @GET("trip-handler/shipments/unassigned-shipment-count")
    fun unAssignedCount() : Observable<UnassignedCount>

    /**
     * Get Current active audit details
     */
    @GET("trip-handler/trips/status-count?")
    fun getTabCount(@Query("from") startDate: String, @Query("to") endDate: String) : Observable<List<TripCount>>

    /**
     * Get Current active audit details
     */
    @POST("trip-handler/shipments/unblock")
    fun unBlockShipment(@Body deleteShipment: DeleteShipment) : Observable<CommonResponse>

    /**
     * Get Current active audit details
     */
    @POST("trip-handler/settlement/v3/trip/{tripId}/initiate")
    fun getTripSettlementDetails(@Path("tripId") tripId: Long, @Query("receiver") receiver: String = "FLASH_WEB") : Observable<TripSettlementDTO>

    /**
     * Get Returned Itens
     */
    @POST("trip-handler/settlement/trip/{tripId}/items/returned")
    fun getReturnedItems(@Header("assetId") assetId : Long, @Header("assetName") assetName : Long, @Path("tripId") tripId : Long,@Body request : DispatchShipmentRequest) : Observable<List<Item>>

    /**
     * Get Returned Itens
     */
    @POST("trip-handler/settlement/v2/trip/{tripId}/done")
    fun settleTrip(@Header("assetId") assetId : Long, @Header("assetName") assetName : Long, @Path("tripId") tripId : Long, @Body request : TripSettlementCompleteDTO, @Query("asset") asset: String ) : Observable<CommonResponse>

    /**
     * Get Returned Itens
     */
    @POST("trip-handler/settlement/v3/trip/{tripId}/done")
    fun settleTripV3(@Header("assetId") assetId : Long, @Header("assetName") assetName : Long, @Path("tripId") tripId : Long, @Body request : TripSettlementCompleteDTO, @Query("asset") asset: String, @Query("receiver") receiver: String = "FLASH_WEB" ) : Observable<TripSettlementResponse>

    /**
     * Create Smart Trip for zones
     */
    @POST("trip-handler/trips/createSmartTrip")
    fun createSmartTrip(@Header("assetId") assetId : Long, @Header("assetName") assetName : Long,@Body request : ZoneSprinterDetailsRequest) : Observable<CommonResponse>

    @GET("trip-service/trips/smartTripProcess/v2")
    fun smartTripProcess(@Header("assetId") assetId : Long, @Header("assetName") assetName : Long) : Observable<List<SmartTripResponse>>

    @POST("trip-handler/trips/cancelSmartripProcess/{id}")
    fun cancelSmartTrip(@Header("assetId") assetId : Long, @Header("assetName") assetName : Long, @Path("id") id : Int) : Observable<CommonResponse>

    /* Get Balances
     */
    @GET("trip-handler/cashclosing/balances")
    fun getBalances(@Header("assetId") assetId : Long, @Header("assetName") assetName : Long) : Observable<BalancesDTO>

    /**
     * Get CashClosing
     * page=0&size=10&entityId=1
     */
    @GET("trip-handler/cashclosing")
    fun getCashClosingDetails(@Header("assetId") assetId : Long,
                              @Header("assetName") assetName : Long,
                              @Query("page")page: Int,
                              @Query("size")size: Int,
                              @Query("entityId")entityId: Int) : Observable<CashClosingDetails>

    /**
     * Upload Image
     */
    @Multipart
    @POST("trip-handler/upload")
    fun uploadCashImage(@Part file: MultipartBody.Part): Single<ImageUploadDetails>

    /**
     * Create Cash Closing Summary
     */
    @POST("trip-handler/accounts/cashclosing/create/v3")
    fun createCashSummaryV2(@Body cashRequest : CashClosingRequest,@Header("assetId") assetId : Long, @Header("assetName") assetName : String) : Single<ResponseBody>

    /**
     * Create Cash Closing Summary
     */
    @POST("trip-handler/accounts/cashclosing/create/v4")
    fun createCashSummaryV4(@Body cashRequest : CashClosingRequest,@Header("assetId") assetId : Long, @Header("assetName") assetName : String) : Single<ResponseBody>

    /**
     * get url invoice to print v2
     */
    @POST("trip-handler/invoice/invoiceDetails")
    fun getInvoiceUrlV2(@Header("assetId") assetId : Long, @Header("assetName") assetName : String,@Body invoiceDetails : InvoiceDetailRequest) : Single<Invoice>

    @GET("trip-handler/invoice/invoiceDetails/v2")
    fun getInvoiceUrlV3(@Header("assetId") assetId : Long, @Header("assetName") assetName : String, @Query("invoiceId") invoiceId: String) : Single<Invoice>
    /*
     * Verify pincode
     */
    @GET("trip-handler/shipments/pincode-verification")
    fun verifyPincode(@Query("pincode") pincode: String,
                      @Query("shipmentId") shipmentId: String) : Observable<PincodeVerify>

    /**
     * Update Pincode
     */
    @POST("trip-handler/shipments/pincode-update")
    fun updatePincode(@Body updatePincode: UpdatePincode) : Observable<CommonResponse>


    @POST("trip-handler/trips/expectedTripsWithForwardBags")
    fun getInTransitTrips(@Header("assetId") assetId : Long, @Header("assetName") assetName : String) : Single<List<InTransitTrip>>

    /**
     * Update Pincode
     */
    @POST("trip-service/trips/mergeCreatedTrips")
    fun mergeCreatedTrips(@Header("assetId") assetId : Long, @Header("assetName") assetName : String,@Body request: MergeCreatedTripRequest) : Single<ResponseBody>

    /**
     * Update Pincode
     */
    @GET("trip-service/shipments/address-details/{shipmentId}")
    fun viewAddressDetails(@Path("shipmentId") shipmentId: String) : Single<AddressDTO>

    @POST("trip-handler/trips/assignFmShipment")
    fun getFmPickupShipment(@Query("barcode")barcode: String,@Query("tripId") tripId: Long): Single<FmPickedShipment>

    /**
    * Create Smart Trip for zones
    */
    @POST("trip-handler/trips/createSmartTrip/v1")
    fun createSmartTripV1(@Header("assetId") assetId : Long, @Header("assetName") assetName : Long,@Body request : SmartTripCreateRequest) : Observable<CommonResponse>

    /**
     * Get Cash collection breakup
     * @param assetId
     * @param cashClosingId
     */
    @GET("trip-handler/cashclosing/get-cash-collection-breakup")
    fun getCashCollectionBreakup(@Query("assetId") assetId: Long, @Query("cashClosingId") cashClosingId : String?, @Query("page") page: Int, @Query("size") size: Int) : Observable<CashCollectionBreakupResponse>

    /**
     * Update Sprinter Vehicle Number
     */
    @POST("trip-handler/users/update-vehicle-number")
    fun updateVehicleNumber(@Query("userId")id: String,@Query("vehicleNumber") number: String): Observable<Boolean>

    /**
     * Get Health Status
     */
    @GET("user-service/system/maintenance-mode-status")
    fun checkHealthStatus(): Observable<Boolean>

    /**
     * Get Inward Runs
     * @header assetId
     */
    @GET("dispatch-handler-service/inward-runs")
    fun getInwardRuns(@Header("assetId") assetId: Long,@Header("assetName") assetName : String,@Query("dataForNumOfDays") dataForNumOfDays: Int) : Observable<InwardRunResponse>

    /**
     * Get Inward Run Details for run id
     * @param runId
     */
    @GET("dispatch-handler-service/inward-runs/{runId}")
    fun getInwardRunItemsForRunId(@Header("assetId") assetId: Long, @Header("assetName") assetName : String, @Path("runId") runId: Int): Observable<InwardRun>

    /**
     * Get Shipment Details for barcode
     * @param barcode
     */
    @GET("dispatch-handler-service/shipments/scan")
    fun getShipmentDetails(@Header("assetId") assetId: Long, @Header("assetName") assetName : String, @Query("barcode") barcode: String, @Query("partnerId") partnerId: Int?): Observable<ReceivingShipmentDTO?>

    /**
     * Create Inward Run
     */
    @POST("dispatch-handler-service/inward-runs")
    fun createInwardRun(@Header("assetId") assetId: Long, @Header("assetName") assetName : String, @Body createInwardRequest: CreateInwardRequest): Observable<InwardRun>

    /**
     * Create Inward Run Item for runId
     * @param runId
     * @param createInwardRunItemRequest
     */
    @POST("dispatch-handler-service/inward-runs/{runId}/inward-run-items")
    fun createInwardRunItem(@Header("assetId") assetId: Long, @Header("assetName") assetName : String, @Path("runId")runId: Int, @Body createInwardRunItemRequest: CreateInwardRunItemRequest): Observable<InwardRunItem>

    /**
     * Get Exception Reasons
     */
    @GET("dispatch-handler-service/inward-runs/exception-reasons")
    fun getExceptionReasonsForShipment(@Header("assetId") assetId: Long, @Header("assetName") assetName : String, @Query("shipmentId") shipmentId: Int): Observable<ExceptionReasonResponse>

    /**
     * Update status and reason
     */
    @PUT("dispatch-handler-service/inward-runs/{runId}/inward-run-items/{runItemId}")
    fun updateInwardRunItem(@Header("assetId") assetId: Long, @Header("assetName") assetName : String, @Path("runId") runId: Int, @Path("runItemId")runItemId: Int, @Body updateInwardRunItemRequest: UpdateInwardRunItemRequest) : Observable<ResponseBody>

    /**
     * Complete Inward Run
     */
    @PUT("dispatch-handler-service/inward-runs/{runId}")
    fun completeInwardRun(@Header("assetId") assetId: Long, @Header("assetName") assetName : String, @Path("runId") runId: Int, @Body request: CompleteInwardRunRequest ) : Observable<ResponseBody>

    /**
     * Get Shipment count for MPS inward items
     */
    @POST("dispatch-handler-service/shipments/get-mps-shipment-count")
    fun getShipmentCountForMpsInwardItems(@Header("assetId") assetId: Long, @Header("assetName") assetName : String, @Body request : MpsShipmentCountRequest): Observable<List<MpsShipmentCountDTO>>

    /**
     * enter number with number sign in
     * @param credentials [ Credentials to be passed for logged in]
     * */
    @POST("user-service/auth/v2/numberSignIn")
    fun numberSignInV2(@Body credentials: Credentials): Single<Profile>


    /**
     * login api with otp user-service
     * @param credentials [ Credentials to be passed for logged in]
     * */
    @POST("user-service/auth/numberSignIn/verify")
    fun otpLogin(@Body credentials: Credentials): Single<Profile>

    @POST("sortation-service/sortation/single_scan")
    fun getSortationBinForSingleScan(@Header("assetId") aseetId : Long, @Header("assetName") assetName : String,@Body order : SortOrder): Single<PackageDto>


    @GET("trip-handler/settlement/upload")
    fun getPreSignedUrl(): Observable<PreSignedUrlResponse>

    @GET("trip-handler/trips/pre-signed-url")
    fun getPreSignedUrlV1(@Query("requestFor") requestFor: String?): Observable<PreSignedUrlResponse>

    @PUT
    fun uploadAckSlip(
        @Header("Content-Type") contentType: String,
        @Url uploadUrl: String,
        @Body body: RequestBody,
        @Header("isAuthorizable") isAuthorizable: Boolean = false
    ): Observable<ResponseBody>


    /**
     * Get Bag Detail by ID
     * @param bagId
     */
    @GET("trip-handler/bagging/getBagDetailsToAddToTrip")
    fun getBagDetailsToAddToTrip(@Header("assetId") assetId : Long, @Header("assetName") assetName : String,@Query("bagId") bagId : String) : Single<BagDTO>

    /**
     * Create Smart Trip for zones
     */
    @POST("trip-handler/trips/createSmartTrip/v2")
    fun createSmartTripV2(@Header("assetId") assetId : Long, @Header("assetName") assetName : Long,@Body request : SmartTripCreateRequest) : Observable<CommonResponse>

    /**
     * Get Cash collection breakup
     * @param assetId
     * @param cashClosingId
     */
    @GET("trip-handler/cashclosing/get-adhoc-transactions-breakup")
    fun getAdhocCashCollectionBreakup(@Query("assetId") assetId: Long, @Query("cashClosingId") cashClosingId : String?, @Query("page") page: Int, @Query("size") size: Int) : Observable<AdhocCashCollectionBreakupResponse>

    /**
     * API to get max tasks per sprinter for given zones
     * @param zoneIds
     *
     * returns Map<Long, Long> -> map of zoneId to maxTasksPerSprinter
     */
    @GET("trip-handler/zone/maxTasksPerSprinter")
    fun getMaxTasksPerSprinter(@Query("zoneIds") zoneIds: List<Int>): Observable<Map<Int, Int>>

    @POST("trip-handler/midmile/runsheet/list")
    fun getConsolidatedManifest(@Header("assetId") assetId : Long, @Header("assetName") assetName : String, @Body request: ConsolidatedManifestRequest): Observable<List<MidMileDispatchedRunsheet>>

    @POST("trip-handler/trips/routeIdBasedTrip")
    fun closeBin(@Header("assetId") assetId : String,
                 @Header("assetName") assetName : String, @Body closeBinRequest: RouteIdBasedTripDto): Observable<ResponseBody>

}