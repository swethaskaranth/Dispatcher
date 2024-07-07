package com.pharmeasy.bolt.ui.adapters

import android.content.Context
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.util.Log
import com.goflash.dispatch.ui.interfaces.OnPrintFinishListener
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.URL


class CustomPrintDocumentAdapter(val activity: Context,  val url :String,val printListener: OnPrintFinishListener) : ThreadedPrintDocumentAdapter(activity,printListener){


    override fun buildLayoutJob(
        oldAttributes: PrintAttributes,
        newAttributes: PrintAttributes,
        cancellationSignal: CancellationSignal,
        callback: LayoutResultCallback,
        extras: Bundle
    ): LayoutJob {
        return PdfLayoutJob(
            oldAttributes, newAttributes,
            cancellationSignal, callback, extras
        )
    }

    override fun buildWriteJob(
        pages: Array<PageRange>,
        destination: ParcelFileDescriptor,
        cancellationSignal: CancellationSignal,
        callback: WriteResultCallback,
        ctxt: Context
    ): WriteJob {
        return PdfWriteJob(
            pages, destination, cancellationSignal,
            callback, ctxt,url
        )

    }


    private class PdfLayoutJob constructor(
        oldAttributes: PrintAttributes,
        newAttributes: PrintAttributes,
        cancellationSignal: CancellationSignal,
        callback: PrintDocumentAdapter.LayoutResultCallback, extras: Bundle
    ) : ThreadedPrintDocumentAdapter.LayoutJob(oldAttributes, newAttributes, cancellationSignal, callback, extras) {

        override fun run() {
            if (cancellationSignal.isCanceled) {
                callback.onLayoutCancelled()
            } else {
                val builder = PrintDocumentInfo.Builder("CHANGE ME PLEASE")

                builder.setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
                    .build()

                callback.onLayoutFinished(
                    builder.build(),
                    newAttributes != oldAttributes
                )
            }
        }
    }


    private class PdfWriteJob internal constructor(
        pages: Array<PageRange>, destination: ParcelFileDescriptor,
        cancellationSignal: CancellationSignal,
        callback: PrintDocumentAdapter.WriteResultCallback, ctxt: Context,
        url :String
    ) : ThreadedPrintDocumentAdapter.WriteJob(pages, destination, cancellationSignal, callback, ctxt,url) {

        override fun run() {
            var input: InputStream? = null
            var out: OutputStream? = null

            try {
                input = URL(url).openStream()
                out = FileOutputStream(destination.fileDescriptor)

                val buf = ByteArray(16384)
                var size: Int = input!!.read(buf)

                while ((size ) >= 0 && !cancellationSignal.isCanceled) {
                    out.write(buf, 0, size)
                    size = input.read(buf)
                }

                if (cancellationSignal.isCanceled) {
                    callback.onWriteCancelled()
                } else {
                    callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))

                }
            } catch (e: Exception) {
                callback.onWriteFailed(e.message)
                Log.e(javaClass.simpleName, "Exception printing PDF", e)
            } finally {
                try {
                    input!!.close()
                    out!!.close()

                } catch (e: IOException) {
                    Log.e(
                        javaClass.simpleName,
                        "Exception cleaning up from printing PDF", e
                    )
                }

            }
        }
    }


}