package com.bera.whitehole.data.localdb

import android.content.Context
import android.content.SharedPreferences

object Preferences {

    const val botToken: String = "botToken"
    const val channelId: String = "channelId"
    const val startTabKey: String = "startTab"

    const val isPeriodicPhotoBackupEnabeld: String = "isPeriodicPhotoBackupEnabled"

    private const val prefFile: String = "preferences"
    private lateinit var preferences: SharedPreferences

    fun init(context: Context) {
        preferences = context.getSharedPreferences(prefFile, Context.MODE_PRIVATE)
    }

    fun getBoolean(key: String, defValue: Boolean) = preferences.getBoolean(key, defValue)
    fun getString(key: String, defValue: String) = preferences.getString(key, defValue)?: defValue
    fun getFloat(key: String, defValue: Float) = preferences.getFloat(key, defValue)
    fun getLong(key: String, defValue: Long) = preferences.getLong(key, defValue)
    fun edit(action: SharedPreferences.Editor.() -> Unit) {
        preferences.edit().apply(action).apply()
    }
}