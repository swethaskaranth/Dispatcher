package com.goflash.dispatch.util

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.*

class DateSerializer: JsonDeserializer<Calendar?> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): Calendar? {
        val timeStamp = json?.asString
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        format.timeZone = TimeZone.getTimeZone("UTC")
        val cal = Calendar.getInstance()
        timeStamp?.let {
            cal.time = format.parse(timeStamp)
            return cal
        }
        return null
    }
}