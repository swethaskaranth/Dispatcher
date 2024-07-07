package com.goflash.dispatch.util.download

import android.content.Context
import android.util.Log
import androidx.work.ForegroundInfo
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.goflash.dispatch.app_constants.KEY_DOWNLOAD_URL
import com.goflash.dispatch.app_constants.KEY_FILE_URI
import com.goflash.dispatch.util.LiveDataHelper
import com.google.common.util.concurrent.ListenableFuture
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

class DownloadWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {

    val liveDataHelper: LiveDataHelper = LiveDataHelper

    override fun doWork(): Result {
        val url: String? = inputData.getString(KEY_DOWNLOAD_URL)
        val fileUri: String? = inputData.getString(KEY_FILE_URI)

        lateinit var result: Result
        url?.let {downloadUrl ->
            fileUri?.let {filePath ->
                result = downloadAPK(downloadUrl, filePath)
            }
        }

        return result
    }

    fun downloadAPK(url: String, fileUri: String): Result {
        var input: InputStream? = null
        var output: OutputStream? = null
        var connection: HttpURLConnection? = null
        lateinit var result: Result

        try {
            val urlConnection = URL(url)
            connection = urlConnection.openConnection() as HttpURLConnection
            connection.connect()

            val lenghtOfFile = connection.contentLength
            input = connection.inputStream

            input?.let {
                output = FileOutputStream(fileUri)

                val data = ByteArray(1024 * 4)

                var count: Int
                var total = 0L

                do {
                    count = input.read(data)
                    if (count != -1) {
                        total += count
                        val percent = (total*100)/lenghtOfFile
                        liveDataHelper.updatePercentage(percent.toInt())
                        output!!.write(data, 0, count)
                    }
                    else
                        break
                } while (count != -1)
            }

            result = Result.success(workDataOf("FILE_URI" to fileUri))

        } catch (e: Exception) {
            e.message?.let { Log.e("Expection ocurred", it) }
            result = Result.failure()
        } finally {

            output?.close()
            input?.close()
            connection?.disconnect()

        }

        return result
    }

    override fun getForegroundInfoAsync(): ListenableFuture<ForegroundInfo> {
        return super.getForegroundInfoAsync()
    }

}
