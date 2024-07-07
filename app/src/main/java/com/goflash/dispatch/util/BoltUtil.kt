package com.goflash.dispatch.util

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Environment
import android.text.InputFilter
import android.text.Spanned
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.util.Pair
import co.uk.rushorm.core.RushCore
import co.uk.rushorm.core.RushSearch
import com.goflash.dispatch.R
import com.goflash.dispatch.app_constants.INVALID_ACCESS
import com.goflash.dispatch.app_constants.SERVER_ERROR
import com.goflash.dispatch.app_constants.UNABLE_PROCESS
import com.goflash.dispatch.data.TaskListDTO
import com.goflash.dispatch.data.UnassignedDTO
import com.goflash.dispatch.model.Error
import com.goflash.dispatch.model.ShipmentDTO
import com.goflash.dispatch.type.PriorityType
import com.goflash.dispatch.type.TaskStatus
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.adapter.rxjava.HttpException
import java.io.*
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern


/**
 * Created by Ravi on 28/05/19.
 */

private const val tag = "BoltUtils"

//Method to check network availblity
fun isNetworkAvailable(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val netInfo = cm.activeNetworkInfo
    return netInfo != null && netInfo.isConnected
}

val gson by lazy {
    GsonBuilder().create()
}

fun httpErrorMessage(error: Throwable): String? {

    val res = (error as HttpException).response()
    val code = res.code()


    val response = res.errorBody()
    val body = response?.string()
    return if (body != null) {
        Log.e("U", body)
        FirebaseCrashlytics.getInstance().log("httpError:- $body")
        val e = try {
            gson.fromJson(body, Error::class.java)
        }catch (ex: Exception){
            FirebaseCrashlytics.getInstance().log("httpError:- $ex")
            Error(body, body)
        }
        if (e?.error == "INVALID_ACCESS")
            INVALID_ACCESS
        else
            e?.message
    } else if (code == 401 || code == 403) {
        return code.toString()
    } else
        SERVER_ERROR

}

fun getAlbumStorageDir(context: Context, albumName: String): File {
    // Get the directory for the user's public pictures directory.
    val file = File(
        context.filesDir, albumName
    )
    if (!file.mkdirs()) {
        file.mkdir()
    }
    return file
}

public fun hasFlash(contrext: Context): Boolean {
    return contrext.packageManager
        .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)
}

fun getDateString(dateStr: String): String {

    val cal = Calendar.getInstance()
    cal.timeInMillis = dateStr.toLong()

    return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.timeInMillis).replace(" ", "")

}

fun dateRange(start: Long, end: Long): MaterialDatePicker.Builder<Pair<Long, Long>> {

    val constraintsBuilder = CalendarConstraints.Builder()

    val builder = MaterialDatePicker.Builder.dateRangePicker()
    val now = Calendar.getInstance()

    constraintsBuilder.setEnd(now.timeInMillis)

    constraintsBuilder.setValidator(RangeValidator(now.timeInMillis))
    builder.setCalendarConstraints(constraintsBuilder.build())

    if (start == 0L && end == 0L) {
        builder.setSelection(Pair(now.timeInMillis, now.timeInMillis))
    } else
        builder.setSelection(Pair(start, end))


    return builder
}

fun getTimestampString(date: Int): String {
    val calendar = Calendar.getInstance()
    val value = calendar.clone() as Calendar
    value.add(Calendar.DATE, -date)
    return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(value.time).replace(" ", "")
}

fun getTimestampString2(): String {
    val calendar = Calendar.getInstance()
    return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(calendar.time).replace(" ", "")
}

fun getTimestampFutureString(date: Int): String {
    val calendar = Calendar.getInstance()
    val value = calendar.clone() as Calendar
    value.add(Calendar.DATE, +date)
    return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(value.time).replace(" ", "")
}

fun getTimestampStringForMonth(date: Int): String {
    val calendar = Calendar.getInstance()
    val value = calendar.clone() as Calendar
    value.add(Calendar.MONTH, -2)
    return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(value.time).replace(" ", "")
}

fun getDateStringFrom(dateStr: String): String {

    val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    val cal = Calendar.getInstance()
    cal.time = format.parse(dateStr)


    val dateFormat = SimpleDateFormat("hh:mm a, MMM dd")
    val dateforrow = dateFormat.format(cal.time)

    return dateforrow

}

fun getPostponedDateStringFrom(dateStr: String): String {

    val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    val cal = Calendar.getInstance()
    cal.time = format.parse(dateStr)


    val dateFormat = SimpleDateFormat("dd/MM/yyyy")
    val dateforrow = dateFormat.format(cal.time)

    return dateforrow

}

suspend fun Activity.compressImageFile(
    path: String,
    shouldOverride: Boolean = true,
    uri: Uri, context: Context?
): String {
    return withContext(Dispatchers.IO) {
        var scaledBitmap: Bitmap? = null

        try {
            val (hgt, wdt) = getImageHgtWdt(uri)
            try {
                val bm = getBitmapFromUri(uri)
                //Log.d(tag, "original bitmap height${bm?.height} width${bm?.width}")
                //Log.d(tag, "Dynamic height$hgt width$wdt")
            } catch (e: Exception) {
                e.printStackTrace()
            }
            // Part 1: Decode image
            val unscaledBitmap = decodeFile(this@compressImageFile, uri, wdt, hgt, ScalingLogic.FIT)
            if (unscaledBitmap != null) {
                if (!(unscaledBitmap.width <= 800 && unscaledBitmap.height <= 800)) {
                    // Part 2: Scale image
                    scaledBitmap = createScaledBitmap(unscaledBitmap, wdt, hgt, ScalingLogic.FIT)
                } else {
                    scaledBitmap = unscaledBitmap
                }
            }

            // Store to tmp file
            val mFolder = File("$filesDir/Images")
            if (!mFolder.exists()) {
                mFolder.mkdir()
            }

            val tmpFile = File(mFolder.absolutePath, "IMG_${getTimestampString()}.png")

            var fos: FileOutputStream? = null
            try {
                fos = FileOutputStream(tmpFile)
                scaledBitmap?.compress(
                    Bitmap.CompressFormat.PNG,
                    getImageQualityPercent(tmpFile),
                    fos
                )
                fos.flush()
                fos.close()
            } catch (e: FileNotFoundException) {
                e.printStackTrace()

            } catch (e: Exception) {
                e.printStackTrace()
            }

            var compressedPath = ""
            if (tmpFile.exists() && tmpFile.length() > 0) {
                compressedPath = tmpFile.absolutePath
                if (shouldOverride) {
                    val srcFile = File(path)
                    val result = tmpFile.copyTo(srcFile, true)
                    //Log.d(tag, "copied file ${result.absolutePath}")
                    //Log.d(tag, "Delete temp file ${tmpFile.delete()}")
                }
            }

            scaledBitmap?.recycle()

            return@withContext if (shouldOverride) path else compressedPath
        } catch (e: Throwable) {
            e.printStackTrace()
        }

        return@withContext ""
    }

}

@Throws(IOException::class)
fun Context.getBitmapFromUri(uri: Uri, options: BitmapFactory.Options? = null): Bitmap? {
    val parcelFileDescriptor = contentResolver.openFileDescriptor(uri, "r")
    val fileDescriptor = parcelFileDescriptor?.fileDescriptor
    val image: Bitmap? = if (options != null)
        BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options)
    else
        BitmapFactory.decodeFileDescriptor(fileDescriptor)
    parcelFileDescriptor?.close()
    return image
}

fun getTimestampString(): String {
    val date = Calendar.getInstance()
    return SimpleDateFormat("yyyy MM dd hh mm ss", Locale.US).format(date.time).replace(" ", "")
}

fun isValidEmail(target: String?): Boolean {
    return if (target == null) {
        false
    } else {
        Patterns.EMAIL_ADDRESS.matcher(target).matches()
    }
}

fun getPostponedDateString(dateStr: String): Long {

    /*val format = SimpleDateFormat("EEE MMM dd kk:mm:ss zXXX yyyy", Locale.ENGLISH)
    val strDate: Date = format.parse(dateStr)*/

    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS")
    val strDate: Date = dateFormat.parse(dateStr)
    //Log.d("Util", "Time  ${strDate.time} ${System.currentTimeMillis()}")
    return strDate.time

}

fun showPriority(data: UnassignedDTO, context: Context): String? {

    when {
        data.status == TaskStatus.BLOCKED.name -> return context.getString(R.string.blocked_braces)
        data.priorityType == PriorityType.HIGH.name -> return context.getString(R.string.high_priority_braces)
        data.postponedToDate != null -> return context.getString(R.string.postponed)
    }

    return null
}

fun getEvent(data: UnassignedDTO): Boolean {
    if ((data.processingBlocked && data.postponedToDate != null
                && getPostponedDateString(data.postponedToDate!!) > System.currentTimeMillis())
    )
        return true

    return false
}

fun getTimeInMillis(date: Int): Long {
    val calendar = Calendar.getInstance()
    val value = calendar.clone() as Calendar
    value.add(Calendar.DATE, -date)
    return value.time.time
}

fun taskListClear() {
    RushCore.getInstance().delete(
        RushSearch().find(TaskListDTO::class.java)
    )
}

fun getFile(mContext: Context?, documentUri: Uri): File {
    val inputStream = mContext?.contentResolver?.openInputStream(documentUri)
    var file = File("")
    inputStream.use { input ->
        file =
            File(mContext?.cacheDir, System.currentTimeMillis().toString() + ".pdf")
        FileOutputStream(file).use { output ->
            val buffer =
                ByteArray(4 * 1024) // or other buffer size
            var read: Int = -1
            while (input?.read(buffer).also {
                    if (it != null) {
                        read = it
                    }
                } != -1) {
                output.write(buffer, 0, read)
            }
            output.flush()
        }
    }
    return file
}

fun getDecimalFormat(amount: Long): String {
    val formatter = DecimalFormat("#,##,###")

    return formatter.format(amount)
}

fun getDecimalFormat(amount: Double): String {
    val formatter = DecimalFormat("#,##,###.###")

    return formatter.format(amount)
}

fun getTime(dateStr: String): String {
    val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZZZ", Locale.getDefault())
    val cal = Calendar.getInstance()
    cal.time = format.parse(dateStr)!!

    val dateFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    //dateFormat.timeZone = TimeZone.getTimeZone("IST")
    if (dateFormat.format(cal.time) == "00:00 AM")
        return ""

    return dateFormat.format(cal.time).replace("am", "AM").replace("pm", "PM")
}

fun getCurrentDate(): String {
    val calendar = Calendar.getInstance()
    return SimpleDateFormat("dd-MM-yyyy", Locale.US).format(calendar.time).replace(" ", "")
}

fun getCurrentTime(): String {

    val calendar = Calendar.getInstance()
    return SimpleDateFormat("hh aa", Locale.getDefault()).format(calendar.time)

}

fun getDate(dateStr: String): String {

    val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    val cal = Calendar.getInstance()
    cal.time = format.parse(dateStr)!!

    val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    val dateforrow = dateFormat.format(cal.time)

    return dateforrow
}

fun getDate2(dateStr: String): String {
    val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZZ", Locale.getDefault())
    val cal = Calendar.getInstance()
    cal.time = format.parse(dateStr)!!

    val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    val dateforrow = dateFormat.format(cal.time)

    return dateforrow
}

fun getTimeRange(dateStr: String): Boolean {

    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZZZ", Locale.getDefault())
    val sdf2 = SimpleDateFormat("yyyy-MM-dd'T'", Locale.getDefault())

    val cal = Calendar.getInstance()
    val cal2 = Calendar.getInstance()

    val date = sdf2.format(cal.time)
    val timeStart = "$date 00:30:00.000+0000"

    val date2 = sdf2.format(cal2.time)
    val timeEnd = "$date2 16:30:00.000+0000"

    val d1: Date?
    val d2: Date?
    val d3: Date?

    d3 = sdf.parse(dateStr)!!
    d1 = sdf.parse(timeStart)
    d2 = sdf.parse(timeEnd)

    if (d3.after(d1) && d3.before(d2))
        return true

    return false
}

fun getTime1(dateStr: String): Boolean {

    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZZZ", Locale.getDefault())

    val cal = Calendar.getInstance()

    val date = sdf.format(cal.time)

    val d1: Date?
    val d2: Date?

    d2 = sdf.parse(dateStr)
    d1 = sdf.parse(date)

    if (d1.before(d2))
        return true

    return false
}

fun checkBreachTime(dateStr: String): Boolean {

    if ((getCurrentDate() == getDate2(dateStr)) && getTime1(dateStr) && getTimeRange(dateStr))
        return true

    return false
}

fun onlyDigits(str: String?): Boolean {
    // Regex to check string
    // contains only digits
    val regex = "[0-9]+"

    // Compile the ReGex
    val p: Pattern = Pattern.compile(regex)

    // If the string is empty
    // return false
    if (str == null) {
        return false
    }

    // Find match between given string
    // and regular expression
    // using Pattern.matcher()
    val m: Matcher = p.matcher(str)

    // Return if the string
    // matched the ReGex
    return m.matches()
}

/**
 * Checks if the device is rooted.
 *
 * @return <code>true</code> if the device is rooted, <code>false</code> otherwise.
 */
fun isRooted(): Boolean {
    return checkRootMethod1() || checkRootMethod2() || checkRootMethod3()
/*
    // get from build info
    val buildTags = android.os.Build.TAGS;
    if (buildTags != null && buildTags.contains("test-keys")) {
        return true
    }

    // check if /system/app/Superuser.apk is present
    try {
        val file = File("/system/app/Superuser.apk");
        if (file.exists()) {
            return true
        }
    } catch (e1: Exception) {
        // ignore
    }

    // try executing commands
    return canExecuteCommand("/system/xbin/which su")
            //|| canExecuteCommand("/system/bin/which su")
            || canExecuteCommand("which su");*/
}

// executes a command on the system
fun canExecuteCommand(command: String): Boolean {
    return try {
        Runtime.getRuntime().exec(command)
        true
    } catch (e: Exception) {
        false
    }
}

fun checkRootMethod1(): Boolean {
    val buildTags = android.os.Build.TAGS
    return buildTags != null && buildTags.contains("test-keys")
}

fun checkRootMethod2(): Boolean {
    val paths = listOf(
        "/system/app/Superuser.apk",
        "/sbin/su",
        "/system/bin/su",
        "/system/xbin/su",
        "/data/local/xbin/su",
        "/data/local/bin/su",
        "/system/sd/xbin/su",
        "/system/bin/failsafe/su",
        "/data/local/su",
        "/su/bin/su"
    )
    for (path in paths) {
        if (File(path).exists()) return true
    }
    return false
}

fun checkRootMethod3(): Boolean {
    var process: Process? = null
    try {
        process = Runtime.getRuntime().exec(arrayOf("/system/xbin/which", "su"))
        val input = BufferedReader(InputStreamReader(process.inputStream))
        if (input.readLine() != null) return true
        return false
    } catch (t: Throwable) {
        return false
    } finally {
        process?.destroy()
    }
}

fun getTimeFromISODate(dateStr: String): String {

    val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
    val cal = Calendar.getInstance()
    format.timeZone = TimeZone.getTimeZone("IST")
    cal.time = format.parse(dateStr)


    val dateFormat = SimpleDateFormat("dd-MM-yy, hh:mm a")
    val dateforrow = dateFormat.format(cal.time)

    return dateforrow

}

fun getDateTime(dateStr: String): String {

    val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
    val cal = Calendar.getInstance()
    cal.time = format.parse(dateStr)


    val dateFormat = SimpleDateFormat("dd-MM-yy, hh:mm a")
    val dateforrow = dateFormat.format(cal.time)

    return dateforrow


}

fun <T> mutableListWithCapacity(capacity: Int): MutableList<T> =
    ArrayList(capacity)


fun isSameDateAsToday(date: Calendar): Boolean{
    val format = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
    val today = Calendar.getInstance()

    return format.format(today.time).equals(format.format(date.time))
}


fun setSlotForTask(view: TextView, slotStart: Calendar, slotEnd: Calendar) {
    val dateFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    dateFormat.timeZone = TimeZone.getTimeZone("Asia/Kolkata")

    val startHourStr = dateFormat.format(slotStart.time)
    val endHourStr = dateFormat.format(slotEnd.time)
    view.text = "$startHourStr - $endHourStr"

    val currentTime = Calendar.getInstance()
    val currentHour = currentTime.get(Calendar.HOUR_OF_DAY)
    val startHour = slotStart.get(Calendar.HOUR_OF_DAY)
    val endHour = slotEnd.get(Calendar.HOUR_OF_DAY)
    view.setBackgroundResource(if(currentHour in startHour..endHour) R.drawable.slot_background else R.drawable.slot_passed_background)
}

fun setSlotForTaskIfAvailable(timeSlot: TextView, slotStart: Calendar?, slotEnd: Calendar?){
    if(slotStart != null && slotEnd != null){
        if(isSameDateAsToday(slotStart) && isSameDateAsToday(slotEnd)){
            timeSlot.visibility = View.VISIBLE
            setSlotForTask(timeSlot, slotStart, slotEnd)
        }else {
            timeSlot.visibility = View.VISIBLE
            timeSlot.text = "Unavailable"
            timeSlot.setBackgroundResource(R.drawable.slot_passed_background)
        }
    }
    else
        timeSlot.visibility = View.GONE
}

fun <I, O> Activity.registerForActivityResult(
    contract: ActivityResultContract<I, O>,
    callback: ActivityResultCallback<O>
) = (this as ComponentActivity).registerForActivityResult(contract, callback)

fun setEditTextCharacterLimit(editText: EditText, limit: Int) {
    val inputFilters = arrayOf<InputFilter>(InputFilter.LengthFilter(limit), object : InputFilter {
        override fun filter(
            source: CharSequence?,
            start: Int,
            end: Int,
            dest: Spanned?,
            dstart: Int,
            dend: Int
        ): CharSequence? {
            val charactersLeft = limit - (dest?.length ?: 0)
            if (charactersLeft <= 0) {
                return ""
            }
            val newLength = Math.min(charactersLeft, end - start)
            return source?.subSequence(start, start + newLength)
        }
    })
    editText.filters = inputFilters
}

fun getDateFromCurrentDate(days: Int): String{
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DATE, -days)
    return SimpleDateFormat("dd-MM-yyyy", Locale.US).format(calendar.time).replace(" ", "")
}


