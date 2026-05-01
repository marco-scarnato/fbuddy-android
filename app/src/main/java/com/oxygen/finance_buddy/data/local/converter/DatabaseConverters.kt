package com.oxygen.finance_buddy.data.local.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.oxygen.finance_buddy.data.local.model.AccountStatePayload

class DatabaseConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromAccountStatePayload(payload: AccountStatePayload): String {
        return gson.toJson(payload)
    }

    @TypeConverter
    fun toAccountStatePayload(json: String): AccountStatePayload {
        val type = object : TypeToken<AccountStatePayload>() {}.type
        return gson.fromJson(json, type)
    }
}

