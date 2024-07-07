package com.goflash.dispatch.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Parcelable
import android.provider.MediaStore
import android.widget.Toast
import com.goflash.dispatch.R
import com.goflash.dispatch.app_constants.REQUEST_IMAGE_CAPTURE
import com.goflash.dispatch.app_constants.album_name2
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

fun dispatchTakePictureIntent(context: Activity, uri: Uri) {

    var chooserIntent: Intent? = null

    var intentList: MutableList<Intent> = ArrayList()

    val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)


    val takePhotoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
    takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri)

    intentList = addIntentsToList(context, intentList, pickIntent)
    intentList = addIntentsToList(context, intentList, takePhotoIntent)

    if (intentList.size > 0) {
        chooserIntent = Intent.createChooser(
            intentList.removeAt(intentList.size - 1),
            context.getString(R.string.select_capture_image)
        )
        chooserIntent!!.putExtra(
            Intent.EXTRA_INITIAL_INTENTS,
            intentList.toTypedArray<Parcelable>()
        )
    }

    context.startActivityForResult(chooserIntent, REQUEST_IMAGE_CAPTURE)
}

fun addIntentsToList(
    context: Context,
    list: MutableList<Intent>,
    intent: Intent
): MutableList<Intent> {
    val resInfo = context.packageManager.queryIntentActivities(intent, 0)
    for (resolveInfo in resInfo) {
        val packageName = resolveInfo.activityInfo.packageName
        val targetedIntent = Intent(intent)
        targetedIntent.setPackage(packageName)
        list.add(targetedIntent)
    }
    return list
}

@Throws(IOException::class)
fun createImageFile(context: Context, albumName: String): File {
    // Create an image file name
    val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())

    val storageDir =
        File(context.filesDir, albumName)

    if (!storageDir.mkdirs())
        storageDir.mkdir()

    val f = File.createTempFile(
        "JPEG_${timeStamp}_", /* prefix */
        ".jpg", /* suffix */
        storageDir /* directory */
    )

    return f
}

fun compressImage(context: Context,path: String, output: File, uri: Uri) {
    val (hgt, wdt) = context.getImageHgtWdt(uri)
    val bmp = decodeFile(context, uri, hgt, wdt, ScalingLogic.FIT)
    if (bmp != null) {
        val stream = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.JPEG, 90, stream)
        val fos = FileOutputStream(output)
        fos.write(stream.toByteArray())
        fos.flush()
        fos.close()
        stream.close()
    }
}

fun deleteTempFiles(context: Context, albumName: String) {
    val file = File(context.filesDir, albumName)
    if (file.exists()) {
        val entries = file.list()
        for (s in entries) {
            val currentFile = File(file.path, s)
            currentFile.delete()
        }
    }

    file.delete()
}

fun deleteFile(context: Context, albumName: String, fileName: String) {
    val file = File(getAlbumStorageDir(context, albumName), fileName)
    if (file.exists()) {
        file.delete()
    }
}

fun getAllFilesInDir(context: Context, albumName: String): List<String>{
    val directory = File(albumName)
    val fileNames = mutableListOf<String>()

    if (directory.isDirectory) {
        val files = directory.listFiles()

        if (files != null) {
            for (file in files) {
                if (file.isFile) {
                    fileNames.add(File(
                        getAlbumStorageDir(context, album_name2),
                        file.name
                    ).toString())
                }
            }
        }
    }

    return fileNames
}
