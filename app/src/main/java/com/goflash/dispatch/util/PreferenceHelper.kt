package com.goflash.dispatch.util

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

object PreferenceHelper {

    private const val NAME = "DISPATCHER"
    private const val MODE = Context.MODE_PRIVATE
    private lateinit var preferences: SharedPreferences

    // list of app specific preferences
    private val authorization = "Authorization"
    private val trip_id = "trip_id"
    private val fcm_token = "fcm_token"
    private val start_date = "start_date"
    private val end_date = "end_date"
    private val agent_name = "agentName"
    private val route_Id = "routeId"

    private val assetName = "assetName"
    private val assetId = "assetID"

    private val update_type = "update_type"
    private val data_for_num_days = "dataForNumDays"
    private val mobile_number = "mobile_number"

    private val single_scan_sortation = "single_scan_sortation"
    private val invoice_generation_flag = "invoiceGenerationFlag"

    fun init(context: Context) {
        val keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC
        val mainKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec)
        preferences = EncryptedSharedPreferences.create(NAME,mainKeyAlias,context,EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM)
        //preferences = context.getSharedPreferences(NAME, MODE)
    }

    /**
     * SharedPreferences extension function, so we won't need to call edit() and apply()
     * ourselves on every SharedPreferences operation.
     */
    private inline fun SharedPreferences.edit(operation: (SharedPreferences.Editor) -> Unit) {
        val editor = edit()
        operation(editor)
        editor.apply()
    }

    var token: String
        get() = preferences.getString(authorization, "") ?: ""
        set(value) = preferences.edit {
            it.putString(authorization, value)
        }

    var tripId: Int
        get() = preferences.getInt(trip_id, 0)
        set(value) = preferences.edit {
            it.putInt(trip_id, value)
        }

    var startDate: String
        get() = preferences.getString(start_date, "") ?: ""
        set(value) = preferences.edit {
            it.putString(start_date, value)
        }

    var endDate: String
        get() = preferences.getString(end_date, "") ?: ""
        set(value) = preferences.edit {
            it.putString(end_date, value)
        }

    var agentName: String?
        get() = preferences.getString(agent_name, null)
        set(value) = preferences.edit {
            it.putString(agent_name, value)
        }

    var updateType: String
        get() = preferences.getString(update_type, "") ?: ""
        set(value) = preferences.edit {
            it.putString(update_type, value)
        }

    var routeId: String?
        get() = preferences.getString(route_Id, "") ?: ""
        set(value) = preferences.edit {
            it.putString(route_Id, value)
        }

    var dataForNumDays: Int
        get() = preferences.getInt(data_for_num_days, 0)
        set(value) = preferences.edit {
            it.putInt(data_for_num_days, value)
        }

    var assignedAssetName: String
        get() = preferences.getString(assetName, "") ?: ""
        set(value) = preferences.edit {
            it.putString(assetName, value)
        }
    var assignedAssetId: Long
        get() = preferences.getLong(assetId, 0)
        set(value) = preferences.edit {
            it.putLong(assetId, value)
        }

    var mobileNumber: String
        get() = preferences.getString(mobile_number, "") ?: ""
        set(value) = preferences.edit {
            it.putString(mobile_number, value)
        }

    var singleScanSortation: Boolean
        get() = preferences.getBoolean(single_scan_sortation, false)
        set(value) = preferences.edit {
            it.putBoolean(single_scan_sortation, value)
        }

    var invoiceGenerationFlag: Boolean
        get() = preferences.getBoolean(invoice_generation_flag, false)
        set(value) = preferences.edit {
            it.putBoolean(invoice_generation_flag, value)
        }
}