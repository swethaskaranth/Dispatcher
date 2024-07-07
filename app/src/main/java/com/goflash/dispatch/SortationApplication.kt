package com.goflash.dispatch

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import co.uk.rushorm.android.AndroidInitializeConfig
import co.uk.rushorm.core.Rush
import co.uk.rushorm.core.RushCore
import com.goflash.dispatch.app_constants.model
import com.goflash.dispatch.data.*
import com.goflash.dispatch.di.components.DaggerNetworkComponent
import com.goflash.dispatch.di.components.NetworkComponent
import com.goflash.dispatch.di.module.NetworkModule
import com.goflash.dispatch.model.BagDetails
import com.goflash.dispatch.scanner.ScannerService
import com.goflash.dispatch.util.PreferenceHelper
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.instana.android.Instana
import com.instana.android.core.InstanaConfig
import com.pharmeasy.barcode.BarcodeReader
import javax.inject.Inject

/**
 * Created by Ravi on 28/05/19.
 */
class SortationApplication : Application() {

    @Inject
    lateinit var networkModule: NetworkModule

    private var networkComponent: NetworkComponent? = null
  //  private var networkComponent2: NetworkComponent? = null

    private var barcodeReader: BarcodeReader? = null


    override fun onCreate() {
        super.onCreate()

        mContext = applicationContext

        PreferenceHelper.init(this)

        initDagger()
        initRushOrm()

        model = Build.MANUFACTURER+" "+ Build.MODEL

        //setUpCrashlytics()

        barcodeReader = BarcodeReader.getInstance(this)

        setupInstana()
    }

    override fun onTerminate() {
        super.onTerminate()
        ScannerService.disconnect()
    }

    private fun initRushOrm() {

        val classes = mutableListOf<Class<out Rush>>()
        classes.add(PackageDto::class.java)
        classes.add(ScannedOrder::class.java)
        classes.add(BagDTO::class.java)
        classes.add(ReceivingDto::class.java)
        classes.add(VehicleDetails::class.java)
        classes.add(BagDetails::class.java)
        classes.add(TripList::class.java)
        classes.add(TripDTO::class.java)
        classes.add(TaskListDTO::class.java)
        classes.add(TripSettlementDTO::class.java)
        classes.add(UndeliveredShipmentDTO::class.java)
        classes.add(ReturnShipmentDTO::class.java)
        classes.add(Item::class.java)
        classes.add(ReceiveCashDTO::class.java)
        classes.add(ReceiveChequeDTO::class.java)
        classes.add(CashChequeCollectedDetailsDTO::class.java)
        classes.add(SprinterForZone::class.java)
        classes.add(ExpenseDTO::class.java)
        classes.add(CashPickupDTO::class.java)
        classes.add(SprinterForZone::class.java)
        classes.add(ReceiveCdsCash::class.java)
        classes.add(CdsCashCollection::class.java)
        classes.add(LostDamagedShipment::class.java)
        classes.add(FmPickedShipment::class.java)
        classes.add(FmPickedupShipmentsDTO::class.java)
        classes.add(ReceiveNetBankDTO::class.java)
        classes.add(ReceivingShipmentDTO::class.java)
        classes.add(ReceivingChildShipmentDTO::class.java)
        classes.add(ReceiveShipmentConfig::class.java)
        classes.add(AckForRecon::class.java)
        classes.add(AckSlipDto::class.java)
        classes.add(ReconImageDTO::class.java)
        classes.add(ReturnReconImageDTO::class.java)
        classes.add(PoaResponseForRecon::class.java)
        classes.add(PoaResponse::class.java)
        classes.add(Poa::class.java)
        classes.add(MetaDetails::class.java)
        classes.add(PackageCountDTO::class.java)
        val config = AndroidInitializeConfig(applicationContext, classes)
        RushCore.initialize(config)
    }

    private fun initLeakCanary() {
        /* if (LeakCanary.isInAnalyzerProcess(this)) {
             return
         }
         LeakCanary.install(this)*/
    }

    fun initDagger() {
        networkComponent = DaggerNetworkComponent.builder()
                .networkModule(NetworkModule(BuildConfig.API_URL, applicationContext))
                .build()

    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var mContext: Context? = null

        fun getApplicationCtx(): Context {
            return mContext!!
        }

        fun getSortationApplicationClass(): SortationApplication {
            return mContext as SortationApplication

        }
    }

    fun getNetworkComponent(): NetworkComponent? {
        return networkComponent
    }

    /*fun getNetworkComponent2(): NetworkComponent? {
        return networkComponent2
    }*/

    private fun setUpCrashlytics(){
        FirebaseApp.initializeApp(this)
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
    }

    fun getBarcodeReader(): BarcodeReader {
        return barcodeReader!!
    }

    fun enableScanning() {
        barcodeReader?.enableScanner()

    }

    fun disableScanning() {
        barcodeReader?.disableScanner()
    }

    fun resume() {
        barcodeReader?.onResume()
    }

    fun pause() {
        barcodeReader?.onPause()
    }

    private fun setupInstana(){
        Instana.setup(this, InstanaConfig(key = BuildConfig.INSTANA_KEY, reportingURL = BuildConfig.INSTANA_REPORTING_URL))
    }
}