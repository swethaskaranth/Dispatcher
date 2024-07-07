package com.goflash.dispatch.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.SystemClock
import android.print.PrintJobInfo
import android.print.PrintManager
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


class PrintJobMonitorService : Service(),Runnable{

    private val POLL_PERIOD : Long = 3
    private var mgr: PrintManager? = null
    private val executor = Executors.newSingleThreadScheduledExecutor()
    private var lastPrintJobTime = SystemClock.elapsedRealtime()


    override fun onCreate() {
        super.onCreate()

        mgr = getSystemService(Context.PRINT_SERVICE) as PrintManager
        executor.scheduleAtFixedRate(
            this, POLL_PERIOD, POLL_PERIOD,
            TimeUnit.SECONDS
        )
    }

    override fun onDestroy() {
        executor.shutdown()

        super.onDestroy()
    }

    override fun run() {
        for (job in mgr!!.getPrintJobs()) {
            if (job.info.state == PrintJobInfo.STATE_CREATED
                || job.isQueued() || job.isStarted()
            ) {
                lastPrintJobTime = SystemClock.elapsedRealtime()
            }
        }

        val delta = SystemClock.elapsedRealtime() - lastPrintJobTime

        if (delta > POLL_PERIOD * 2) {
            stopSelf()
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}