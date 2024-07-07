package com.pharmeasy.bolt.ui.adapters

import android.content.Context
import android.print.PrintDocumentAdapter
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.os.Bundle
import android.print.PrintAttributes
import android.os.CancellationSignal
import com.goflash.dispatch.ui.interfaces.OnPrintFinishListener
import java.util.concurrent.Executors


abstract class ThreadedPrintDocumentAdapter(val context: Context, val listener : OnPrintFinishListener) : PrintDocumentAdapter(){

     abstract fun buildLayoutJob(
        oldAttributes: PrintAttributes,
        newAttributes: PrintAttributes,
        cancellationSignal: CancellationSignal,
        callback: LayoutResultCallback,
        extras: Bundle
    ): LayoutJob

     abstract fun buildWriteJob(
        pages: Array<PageRange>,
        destination: ParcelFileDescriptor,
        cancellationSignal: CancellationSignal,
        callback: WriteResultCallback,
        ctxt: Context
    ): WriteJob

    private val threadPool = Executors.newFixedThreadPool(1)

    override fun onLayout(
        oldAttributes: PrintAttributes?,
        newAttributes: PrintAttributes?,
        cancellationSignal: CancellationSignal?,
        callback: LayoutResultCallback?,
        extras: Bundle?
    ) {
        threadPool.submit(buildLayoutJob(oldAttributes!!, newAttributes!!,
            cancellationSignal!!, callback!!,
            extras!!));

    }

    override fun onWrite(
        pages: Array<PageRange>?,
        destination: ParcelFileDescriptor?,
        cancellationSignal: android.os.CancellationSignal?,
        callback: WriteResultCallback?
    ) {
        threadPool.submit(buildWriteJob(pages!!, destination!!,
            cancellationSignal!!, callback!!, context));

    }

    override fun onFinish() {
        threadPool.shutdown()
        listener.onPrintFinished()
       // super.onFinish()
    }

    abstract class LayoutJob constructor(
         var oldAttributes: PrintAttributes,
         var newAttributes: PrintAttributes,
         var cancellationSignal: CancellationSignal,
         var callback: PrintDocumentAdapter.LayoutResultCallback,  var extras: Bundle
    ) : Runnable


    abstract class WriteJob constructor(
        var pages: Array<PageRange>,  var destination: ParcelFileDescriptor,
        var cancellationSignal: CancellationSignal,
        var callback: PrintDocumentAdapter.WriteResultCallback,  var ctxt: Context,
         var url : String
    ) : Runnable


}